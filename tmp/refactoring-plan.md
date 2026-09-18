# tg-bot Refactoring Plan

Working document for agents. Each task is self-contained: it names the files, the problem, the
target state, and its acceptance criteria. Tick the checkbox when the task is done **and** the build
is green.

## How to use this document

1. Pick a task. Respect the `Depends on` field — some tasks are blocked by others.
2. Read the referenced files before changing anything. Line numbers are from the state of the
   `refactoring` branch at the time of writing; they will drift, so match on code, not on numbers.
3. **Stay inside the task scope.** Do not opportunistically "fix" things belonging to another task —
   that destroys the reviewability this document exists for.
4. Run `./gradlew build` after every task (per `AGENTS.md`). `compileKotlin` alone is not enough.
5. Do not commit. The author reviews each task manually.
6. Do not add comments or KDoc (see global rules). Names and structure carry the meaning.
7. When a task changes an invariant recorded in `AGENTS.md`, update `AGENTS.md` in the same task.
8. After task is completed, mark is as done [x]. Later author will remove all the old tasks. 
   Some of the tasks are already deleted.

## Legend

| Field | Meaning |
|---|---|
| **P0** | Blocks the build or is a live security/correctness bug |
| **P1** | Structural debt that keeps producing new debt (the two-formats problem) |
| **P2** | Cleanup, ergonomics, hygiene |
| Size | S = under ~1h, M = a session, L = multi-session, split further if needed |

---

## Executive summary

The project has three distinct debt clusters, in order of cost:

1. **Two parallel update models.** `Update` (raw Telegram DTO) and `UpdateContext` (typed domain
   model) both exist, and almost every handler uses *both* — most commonly by calling the
   `@Deprecated("for migration purposes only!") getUpdate()` escape hatch. Every new feature has to
   choose a side, and they keep choosing both. This is the root cause of most other findings.
2. **Two parallel Telegram DTO trees.** `core/dto/**` (105 files, hand-written Bot API mirror) and
   `core/service/tgapi/TgMessagesDTO.kt` (the `Tg*` types actually used for outgoing calls). 34
   declarations in `core/dto` are provably unreachable, including three exact byte-for-byte
   duplicates of `core/dto/payments/*`.
3. **Blocking persistence in a reactive application.** 23 files inject the blocking `MongoTemplate`
   and call it from `suspend` functions on the WebFlux event loop; `runBlocking` appears inside
   `suspend` functions in three places.

Beyond those: one build-breaking rebase artifact, a fail-open Discord signature check, unauthenticated
debug handlers, an unauthorized child-care admin command, and a CI pipeline that never runs the tests.

Phases 0–7 pay down that debt. **Phase 9** then splits the bots into Gradle modules — which is
cheaper than it looks (only 3 reverse dependencies and 1 cross-feature dependency exist today) and is
worth doing because it makes the dependency direction compiler-enforced. **Phase 10** covers the
separate-repositories idea and argues against it for now, with explicit trigger conditions.

---

# Phase 0 — Unblock the build

### [x] T0.1 — Fix the rebase artifact in `TgMessageService` (P0, S) — DONE

**Files:** `src/main/kotlin/com/nikichxp/tgbot/core/service/tgapi/TgMessageService.kt`

**Root cause (confirmed via git):** commit `b85de1e` ("refactor message edits") renamed
`updateMessageText` → `editMessageText` *and* added two `editMessageText` overloads. During the
rebase onto `master` the rename hunk was dropped while the added overloads survived. The result is
that the 5-parameter implementation is still called `updateMessageText` (line ~74) while three call
sites expect it to be called `editMessageText`:

- `TgMessageService.kt:105` — `editMessageText(chatId, messageId, text, tgBotInfo, replyMarkup)`
- `TgMessageService.kt:118` — same call
- `debug/TestCommandHandler.kt:111` — named arguments `chatId = …, messageId = …, bot = …`

**Do:** rename `suspend fun updateMessageText(` to `suspend fun editMessageText(`. That is the whole
fix — the overload set then resolves (5-arg with `bot`, 4-arg without, 2-arg text-only) and
`TestCommandHandler`'s `bot =` named argument binds to the 5-arg overload.

Do **not** "fix" this by renaming the call sites to `updateMessageText`: `editMessageText` is the
Telegram Bot API method name and is what the branch intended.

**Acceptance:** `./gradlew build` passes. No other files touched.

**Note:** the commented-out reference at `childcarebot/state/AddCurrentSleepTimeJob.kt:42`
(`tgOperations.updateMessageText(`) is dead code handled by T1.5 — ignore it here.

---

# Phase 1 — Delete dead weight

Low-risk, high-signal. Do these before the structural work so later refactors have less surface area.

### [x] T1.1 — Delete unreachable `core/dto` declarations (P2, M) — DONE

**Depends on:** T0.1

`core/dto` is 105 of 241 Kotlin files (~44% of the codebase) and exists only to deserialise
incoming `Update` payloads. Reachability analysis from the real roots (`Update`, `Message`, `User`,
`TgGetFileResponse` — the only `core/dto` types referenced from outside the package) shows **34 of
124 declarations are unreachable**.

Delete these files entirely (every declaration in them is unreachable):

```
core/dto/BotCommand.kt                      core/dto/ParseMode.kt
core/dto/CallbackGame.kt                    core/dto/ReplyKeyboardRemove.kt
core/dto/ChatAction.kt                      core/dto/ResponseParameters.kt
core/dto/ChatId.kt                          core/dto/UserProfilePhotos.kt
core/dto/ForceReplyMarkup.kt                core/dto/WebhookInfo.kt
core/dto/HideKeyboardReplyMarkup.kt         core/dto/inlinequeryresults/InlineQueryResult.kt
core/dto/KeyboardReplyMarkup.kt             core/dto/inlinequeryresults/InputMessageContent.kt
core/dto/MessageId.kt                       core/dto/inputmedia/InputMedia.kt
core/dto/keyboard/KeyboardButton.kt         core/dto/inputmedia/InputMediaConstants.kt
core/dto/payments/InvoiceDetails.kt         core/dto/stickers/StickerSet.kt
core/dto/payments/LabeledPrice.kt           core/dto/payments/PaymentInvoiceInfo.kt
core/dto/payments/ShippingOption.kt
```

That is 283 lines of `InlineQueryResult`, 112 of `InputMedia`, and the entire dead reply-markup
family — the project never sends any of them, because outgoing markup uses the `Tg*` types in
`core/service/tgapi/TgMessagesDTO.kt` (see T4.3).

`core/dto/ParseMode.kt` also carries `// TODO: Remove modeName attribute and stop using it as a
serialization approach for this enum` — deleting the file resolves that TODO.

**Watch out:** `core/dto/keyboard/KeyboardFields.kt` is only used by the deleted `KeyboardButton.kt`;
delete it too. `core/dto/keyboard/InlineKeyboardButton.kt` **must stay** — `Message.replyMarkup`
uses it, and `TgUpdateContextMapper` reads `inlineKeyboard` to resolve button text.

**Acceptance:** `./gradlew build` passes. `find src/main -path '*core/dto*' -name '*.kt' | wc -l`
drops to ~81. No production behaviour change.

### [x] T1.2 — Remove the duplicated payment DTOs (P2, S) — DONE

**Depends on:** T1.1

Three files in `core/dto/` are byte-for-byte duplicates (modulo KDoc) of files in
`core/dto/payments/`, and the top-level copies are the unused ones:

| Delete | Canonical |
|---|---|
| `core/dto/SuccessfulPayment.kt` | `core/dto/payments/SuccessfulPayment.kt` |
| `core/dto/OrderInfo.kt` | `core/dto/payments/OrderInfo.kt` |
| `core/dto/ShippingAddress.kt` | `core/dto/payments/ShippingAddress.kt` |

`Message.kt:12` already imports the `payments` variant. Confirm no import of the top-level ones
remains, then delete.

**Acceptance:** `./gradlew build` passes.

### [x] T1.4 — Move or delete the one-off migration jobs (P2, S) — DONE, both deleted

**Files:** `core/jobs/UpdateEmojiJob.kt`, `core/jobs/RecalculateKarmaJob.kt`

`UpdateEmojiJob` is entirely commented out (`//@Component`, `//@PostConstruct`) and contains a
`TODO()` inside commented code plus its own TODO admitting it belongs elsewhere:

```
TODO Это надо перенести в исполняемый скрипт
     технически это сопутствующий скрипт, надо держать его где-то в другом месте как часть supportive lib
```

`RecalculateKarmaJob` is `@Profile("dev")`, reads `./messages.json` from the working directory,
spawns a raw `thread { Thread.sleep(1000) }` progress printer and uses `println`. It is a one-shot
data-migration script wearing a Spring bean costume.

**Do:** delete `UpdateEmojiJob.kt`. For `RecalculateKarmaJob`, either delete it or move it out of
`core/` into a clearly-marked `tools/` package that is not component-scanned. State which you chose.
This resolves the `UpdateEmojiJob.kt:12` and `:49` TODOs.

**Acceptance:** `./gradlew build` passes. `core/jobs/` no longer contains commented-out beans.

### [x] T1.5 — Remove commented-out code blocks (P2, S) — DONE except `MessageStatHandler.kt:79`, deliberately left for T6.6 (behaviour change)

Commented-out code is the worst kind of dead code: it looks intentional and blocks refactoring
tools. Delete these blocks (do not restore them, do not "improve" them):

| File | Lines | What |
|---|---|---|
| `warehousebot/WarehouseBotCommandHandler.kt` | 37–49 | old `processCommand` / `isCommandSupported` with `runBlocking` |
| `childcarebot/state/AddCurrentSleepTimeJob.kt` | 31–51 | the entire `jobItself()` body — see T1.6 |
| `karmabot/handlers/MessageStatHandler.kt` | 79 | the only actual `sendMessage` in `reportInChat` — see T6.6 |
| `core/handlers/ChatCallbackHandler.kt` | 28 | `// .filter { it.isBotSupported(update.bot) }` |
| `core/handlers/ChatCommandsHandler.kt` | 38 | commented debug log with `// todo create debug config for that` |
| `debug/VersionHandler.kt` | 23–25 | stray blank lines left by removed code |
| `test/…/TGBotApplicationTests.kt` | 6 | `//@SpringBootTest` — see T7.5 |

**Note:** `MessageStatHandler:79` is not purely cosmetic — uncommenting or deleting it changes
behaviour. Handle that one in T6.6 and leave it here only as a cross-reference.

**Acceptance:** `./gradlew build` passes. `grep -rn '^\s*//\s*\(override\|tgOperations\|runBlocking\|@\)' src/main` returns nothing meaningful.

### [x] T1.6 — Resolve the dead child-care scaffolding (P2, S) — DONE, both deleted

**Files:** `childcarebot/state/AddCurrentSleepTimeJob.kt`, `childcarebot/ChildKeyboardProvider.kt`

Two live Spring beans that do nothing or crash:

1. `AddCurrentSleepTimeJob` — `jobItself()` is fully commented out. Its `trackingEntities`
   `mutableMapOf` (line ~22) is written to at start-up and **never read**, so `findLastEvents()` and
   `loadChild()` are pure start-up cost. Delete the bean, or restore the job. Deleting is the
   default; if you restore it, it needs a `@Scheduled` trigger and a test.
2. `InlineKeyboardProvider` (`ChildKeyboardProvider.kt:37-43`) — a registered `@Service` whose only
   method is `TODO("Not yet implemented")`. It is a live `NotImplementedError` waiting for whoever
   removes `@Primary` from `ReplyKeyboardProvider`. Delete it; the `ChildKeyboardProvider`
   abstraction with a single implementation is over-engineering (see T7.4).

**Acceptance:** no `TODO(` calls remain in `src/main`: `grep -rn 'TODO(' src/main` is empty.

---

# Phase 2 — Finish the `UpdateContext` migration

This is the core of the plan. Goal: **delete `UpdateContext.getUpdate()`** and with it the whole
old-format path. Do not attempt this as one commit — T2.1–T2.4 prepare the ground, T2.5.x migrate
module by module, T2.6 removes the escape hatch.

The migration is currently ~40% done and stalled in the worst possible place: the same class often
uses both formats. `voicepad/VoicePadCommandHandler` has `execute(updateContext)` next to
`createPrompt(update: Update)`. `childcarebot/ChildCareCommandHandler.handleUpdate` receives an
`UpdateContext` and immediately calls `getUpdate()`.

### [x] T2.1 — Make `UpdateContext` sufficient (P1, M) — DONE, see Progress log

**Files:** `core/entity/UpdateContext.kt`, `core/entity/common/*.kt`, `core/service/TgUpdateContextMapper.kt`

`getUpdate()` cannot be removed while the typed models are missing data handlers need. Audit of all
`getUpdate()` call sites shows these gaps:

| Missing on the typed model | Needed by |
|---|---|
| `voice` (file id, mime, duration) | `voicepad/VoicePadCommandHandler` |
| `sticker.emoji` | `karmabot/handlers/StickerReplyHandler` |
| chat id / type / title of the *current* chat (not only of the reply) | `karmabot/handlers/MessageStatHandler`, `summary/*` |
| `replyToMessage` presence for non-user replies | `voicepad/VoicePadCommandHandler` |
| markers (`UpdateMarker` set) | `summary/SummaryCommandHandler` (`update.getMarkers()`) |
| typed bot info (`TgBotInfo`, not `BotInfo`) | `debug/TestCommandHandler`, `childcarebot/logic/ChildReportHelper` |

**Do:**

1. Extend `MessageModel` with what is actually consumed: `voice`, `sticker`, `chat` (id, type,
   title). Add a `ChatModel`. Keep it need-driven — do not mirror the whole Bot API again.
2. `MessageModel.id` and `UserModel.id` are `String` while `CallbackModel.userId`/`chatId` are
   `Long` and Telegram ids are `Long`. **Pick `Long`** and make it consistent; the `String` ids
   force `toString()`/`toLong()` round-trips at every boundary and lose type safety.
3. Add `UpdateContext.markers: Set<UpdateMarker>`, populated by `TgUpdateContextMapper`, so
   `Update.getMarkers()` is no longer needed by handlers.
4. Fix `TgUpdateContextMapper.mapCallbackEntity` (lines 79-81): it uses
   `callbackQuery.message!!.replyMarkup?...?.text!!` and `update.getContextChatId()!!` — three `!!`
   in one expression, on data controlled by a remote API. A callback on a message whose keyboard was
   already replaced will NPE the whole update. Make `buttonText` nullable or resolve it lazily.
5. Fix `TgUpdateContextMapper.mapMessageEntity`: it returns `null` when `text == null`, so a
   voice/sticker/photo message has **no** `message` model at all. That is why handlers fall back to
   `getUpdate()`.

**Acceptance:** every field read via `getUpdate()` anywhere in `src/main` has an equivalent on
`UpdateContext`. Write that mapping down in the task result — T2.5.x depends on it.

### [~] T2.2 — Single source of truth for "the mentioned message" (P1, S) — core part DONE; summary copy left for T2.5.3

**Depends on:** T2.1

The `message ?: editedMessage ?: editedChannelPost ?: channelPost ?: callbackQuery.message` fallback
chain is implemented **three times**:

- `core/util/UpdateUtils.kt:59-65` — `Update.getMentionedMessage()`
- `core/entity/UpdateContext.kt:55-61` — `TgUpdateContext.getMentionedMessage()` (identical body,
  different order is easy to introduce by accident)
- `summary/ChatUpdatesToPromptSerializerService.kt:36-37` — inline, and it *omits*
  `callbackQuery.message`

Similarly `TgUpdateContext` re-declares `getContextChatId()` / `getContextUserId()` /
`getContextMessageId()` (lines 51-53) which already exist as `Update` extensions in `UpdateUtils.kt`.

**Do:** keep exactly one implementation, inside `TgUpdateContextMapper`, used only at mapping time.
Delete `TgUpdateContext.getMentionedMessage()`, `TgUpdateContext.getContext*()` and the inline copy
in the summary module. `UpdateUtils.getMentionedMessage()` survives until T2.6 deletes the file.

**Acceptance:** `grep -rn 'editedChannelPost' src/main` returns exactly one location.

### [~] T2.3 — Move the handler SPI to `UpdateContext` (P1, M) — SPI signatures migrated; CommandHandlerExecutor still resolves Update params

**Depends on:** T2.1

The framework interfaces still speak `Update`, which *forces* every implementor to keep the old
format alive. This is the single highest-leverage change in the plan.

| File | Signature to change |
|---|---|
| `core/handlers/Authenticable.kt:6` | `authenticate(update: Update)` → `authenticate(context: UpdateContext)` |
| `core/handlers/UpdateHandler.kt:10` | `canHandle(update: Update)` → `canHandle(context: UpdateContext)` |
| `core/handlers/callbacks/CallbackHandler.kt:9` | `handleCallback(callbackContext, update: Update)` → drop the `update` parameter entirely; `CallbackContext` already carries everything |
| `core/auth/TrustedUserService.kt:28` | `isTrusted(update: Update)` → `isTrusted(context: UpdateContext)` |

Also update the callers in `core/service/UpdateProcessor.kt:54-65` (`isHandlerSupportedForV2` calls
`context.getUpdate()` three times) and `core/handlers/ChatCommandsHandler.kt:47`.

**Do:** change the interfaces, then fix every implementor mechanically. Implementors:
`childcarebot/ChildCare{Callback,Command,DebugCommand}Handler`, `childcarebot/ChildParentsCommandHandler`,
`debug/TestCommandHandler`, `debug/UnparsedMessagesCommandHandler`, `voicepad/VoicePadCommandHandler`,
`warehousebot/WarehouseBotCommandHandler`, `summary/SummaryCommandHandler`.

`CallbackContext` (`core/handlers/callbacks/CallbackContext.kt`) has **three** constructors — one
from `Update`, one from `CallbackModel + UpdateContext`, one from `UpdateContext`. Delete the
`Update` one. Its body is another `!!`-chain duplicating `TgUpdateContextMapper`.

**Acceptance:** `grep -rn 'core.dto.Update' src/main/kotlin/com/nikichxp/tgbot/core/handlers` is empty.

### [ ] T2.4 — Stop the command executor from injecting `Update` (P1, S)

**Depends on:** T2.3

**File:** `core/handlers/commands/CommandHandlerExecutor.kt:25`

The reflective executor resolves an `Update` parameter for any `@HandleCommand` method that asks for
one. As long as it does, handlers will keep asking. Remove the `Update::class.createType()` branch so
that requesting an `Update` becomes a start-up-visible `IllegalStateException("Unknown parameter
type")`.

Do this **after** T2.5.1–T2.5.6 have removed all such parameters, or the app will fail at first
command. Sequence it as the final step of Phase 2 if that is easier.

While in this file: the parameter matching compares `KType`s with `==` against freshly-created
types, which is fragile (nullability and variance make `List<String>?` not match). Consider
`isSubtypeOf` consistently, as the `CommandHandler` branch already does.

**Acceptance:** `CommandHandlerExecutor` has no `Update` import. `./gradlew build` passes and
`ChatCommandTest` still passes (it will need updating — see T7.5).

### [~] T2.5 — Migrate modules off `Update` (P1, L — split per submodule) — in progress, see subtasks

**Depends on:** T2.1, T2.2, T2.3

One checkbox per module so the author can review them independently. In every case the pattern is
the same: delete `import com.nikichxp.tgbot.core.dto.Update`, delete the
`val update = updateContext.getUpdate()` line, and replace `update.getContextX()` with the typed
accessor from T2.1.

#### [~] T2.5.1 — `childcarebot/` — partially done; ChildCareCommandHandler, ChildReplyHandler, ChildReportHelper still on Update

| File | Lines | Legacy usage |
|---|---|---|
| `ChildCareCallbackHandler.kt` | 5, 24-28, 41 | `authenticate(update)`, `handleCallback(…, update)`, `update.getContextUserId()!!` |
| `ChildCareCommandHandler.kt` | 8, 39-40, 55-56, 91-92, 105, 118 | `getUpdate()`, `update.getContextUserId()!!` |
| `ChildCareDebugCommandHandler.kt` | 6, 26, 43-44 | `authenticate(update)`, `getLastEvents(update)` |
| `ChildParentsCommandHandler.kt` | 4, 24-28 | `authenticate(update)`, `update.getContextUserId()!!` |
| `ChildReplyHandler.kt` | 6, 28-42 | `getUpdate()`, `update.message!!` ×3 |
| `logic/ChildReportHelper.kt` | 89-91 | `updateContext.getUpdate().getContextUserId()` |

Note `ChildReportHelper` has both `getChild(UpdateContext)` and `getChild(CallbackContext)` — the
old/new pair. Collapse to one.

#### [ ] T2.5.2 — `karmabot/`

| File | Lines | Legacy usage |
|---|---|---|
| `handlers/GroupChatKarmaHandler.kt` | 3, 29-30, 33, 41, 45-50 | `getUpdate()`, `update.message?.from!!` |
| `handlers/StickerReplyHandler.kt` | 4, 38-40, 44, 54, 64, 68, 76-77 | `getUpdate()`, `update.getMembers()`, `update.convertToMessageIntResult()` |
| `handlers/MessageStatHandler.kt` | 3, 94-97, 101-102 | `getUpdate()`, `getIdAndName(update)` |
| `commands/RegisterEmojiHandler.kt` | 4, 25, 42, 47 | `processCommand(…, update)`, `onEmojiSet(…, update)` |
| `service/actions/LikedMessageService.kt` | 3, 23-24, 49-50, 58 | `changeRating(interaction, update)`, `update.message?.chat?.id!!` |

`GroupChatKarmaHandler:34-38` hand-builds a `MessageInteractionResult` while `StickerReplyHandler`
uses the `Update.convertToMessageIntResult()` extension for the same thing — another old/new pair.
Unify on one factory that takes typed models.

#### [ ] T2.5.3 — `summary/`

| File | Lines | Legacy usage |
|---|---|---|
| `SummaryCommandHandler.kt` | 5, 44-45, 57-58, 95-101, 127, 140 | `getUpdate()` ×3, `getContextChatId()` ×3, `getMarkers()`, `isTrusted(update)` |
| `SummaryMessageStorageService.kt` | 3-4, 21-22 | `storeMessage(update: Update)` |
| `ChatUpdatesToPromptSerializerService.kt` | 3, 31, 36-37 | inline mentioned-message chain |
| `entity/LoggedMessage.kt` | 3-4, 10-11 | **persists the raw `Update` DTO in Mongo** |

`LoggedMessage.update: Update` is the hardest blocker in the whole migration: the Mongo collection
contains serialised `core.dto.Update` documents, so `Update` cannot be changed freely without a data
migration. Options, in order of preference:

1. Store a `LoggedMessageModel` (author id/name, text, date, chatId) going forward and keep a
   read-only legacy path (`DocumentToUpdateConverter` already deserialises `Update` from a `Document`)
   for old records until they age out.
2. Add a `bson`-level migration script. Heavier, and this is a hobby project.

State the choice in the task result — the recap feature reads history, so silently dropping old
records changes behaviour.

`ChatUpdatesToPromptSerializerService.getAuthorNameFromMessage()` (lines 44-50) duplicates
`core/util/UserFormatter.getUserPrintName()`. Delete the local copy.

#### [~] T2.5.4 — `voicepad/` — partially done; createPrompt/createNotepad/deleteVoice still take Update

**File:** `VoicePadCommandHandler.kt` lines 4, 28-29, 36-38, 43-47, 63-66, 71-74, 99-101

This class is the clearest example of the two-formats problem: `execute()` is new-format while
`authenticate(update)`, `canHandle(update)`, `createPrompt(update)`, `createNotepad(update)` and
`deleteVoice(update)` are all old-format, in the same file.

Also: `deleteVoice` calls `sessionService.getActiveSession(chatId)` twice (once to remove, once to
count) — use the value returned by `removeVoice`.

#### [~] T2.5.5 — `debug/` — partially done; NewUserInteractionHandler and log handlers still on getUpdate()

| File | Lines | Legacy usage |
|---|---|---|
| `TestCommandHandler.kt` | 3, 57-66, 69-78, 81-101, 107-119 | four `update: Update` command methods + `handleCallback(…, update)` |
| `UnparsedMessagesCommandHandler.kt` | 5, 13, 35-41 | `authenticate(update)` |
| `interaction/NewUserInteractionHandler.kt` | 3, 8-9, 26-45 | `getUpdate()`, `getContextInvolvedParties()` |
| `log/LogAllMessagesHandler.kt` | 4, 9, 26-35 | `getUpdate()`, and serialises the whole `Update` to the log |
| `log/ViewAllLoggedMessagesHandler.kt` | 4, 11, 24-25 | `configureLogging(args, update)` |

`TestCommandHandler` methods only need `update.bot` — which after T2.1 is
`updateContext.getBotInfo()`, and after T4.2 needs no cast. That also resolves the
`// TODO remove this cast` at line 115.

`NewUserInteractionHandler` builds a display string from `getContextInvolvedParties()` (a
`Map<String, String>`); use the typed `from`/`reply` models instead. See also T2.7.

#### [ ] T2.5.6 — `santabot/` and `warehousebot/`

| File | Lines | Legacy usage |
|---|---|---|
| `santabot/SantaBotCommandHandler.kt` | 3, 8-9, 28, 30, 53, 76, 85, 96, 118, 122, 135, 142, 222-226 | six `update: Update` command methods, `getSantaUserPlayerFromUpdate` |
| `warehousebot/WarehouseBotCommandHandler.kt` | 3, 18-23, 29, 51 | `Map<String, suspend (Update, List<String>) -> Unit>`, `canHandle(update)` |
| `warehousebot/WarehouseService.kt` | 3, 11-12, 17-18 | `list(update)`, `get(update, …)` extract the user id from the raw DTO |

`WarehouseBotCommandHandler` is a special case: its `handleUpdate` is `// TODO implement this` (an
empty body), and the `commands` map it holds is never invoked — the feature is **not wired at all**.
Do not port dead code. See T3.2 for the decision.

### [ ] T2.6 — Delete the deprecated escape hatch (P1, S)

**Depends on:** T2.4, T2.5.1–T2.5.6

The payoff. Delete, in this order:

1. `UpdateContext.getUpdate()` (`core/entity/UpdateContext.kt:16-17`) and its
   `TgUpdateContext` override (line 46, with its `// TODO do everything needed to remove this method`).
2. `TgUpdateContext`'s `private val update: Update` constructor parameter, once nothing reads it.
3. `core/util/UpdateUtils.kt` — the `Update.getContextChatId/MessageId/UserId/UserName`,
   `getContextInvolvedParties`, `getMembers`, `convertToMessageIntResult` extensions and
   `MembersOfUpdate`. Keep `getCurrentUpdateContext()`; move it to its own file
   (`core/util/UpdateContextAccessor.kt`) since it is the only survivor.
4. `Update.bot: TgBotInfo` (`core/dto/Update.kt:40-42`) and its `// TODO think about: maybe move this
   to updateContext, TBD`. A `lateinit var` non-API field bolted onto a wire DTO with `@JsonIgnore`
   is exactly the layering violation this whole phase exists to remove. `TgUpdateContextMapper:19`
   (`update.bot = bot`) sets it; `UpdateContext.getBotInfo()` replaces it.
5. `Update.getMarkers()` in `core/util/Extended.kt`, once `UpdateContext.markers` (T2.1) is in place.
   Keep `Collection.diffWith` — `DocumentToUpdateConverter` uses it.

**Acceptance:**
- `grep -rn 'getUpdate()' src/main` is empty.
- `grep -rn '@Deprecated' src/main` returns only `SpringTgApiCallExecutorImpl` (removed in T3.1) and
  the `InvolvedPartiesFactory` overloads (removed in T2.7).
- `Update` is referenced only by `DocumentToUpdateConverter`, `TgUpdateContextMapper`,
  `TgUpdatePollService` and `UnparsedMessage` — i.e. only at the deserialisation boundary.
- Update `AGENTS.md` to record that `Update` is a wire DTO and must not leave `core/service`.

### [~] T2.7 — Collapse `InvolvedPartiesFactory` into the mapper (P1, S) — file DELETED (was dead); `getContextInvolvedParties` left for T2.5.5

**Depends on:** T2.5.5, T2.6

**Files:** `core/util/InvolvedPartiesFactory.kt`, `core/service/TgUpdateContextMapper.kt`,
`core/util/UpdateUtils.kt`

The same "extract from/reply/message from an update" mapping exists **three times**:

1. `TgUpdateContextMapper.mapFromEntity/mapReplyEntity/mapMessageEntity` (lines 33-71) — the new one.
2. `InvolvedPartiesFactory.createInvolvedParties(update)` (lines 68-106) — `@Deprecated`, and its
   body is a character-for-character copy of (1).
3. `Update.getContextInvolvedParties()` (`UpdateUtils.kt:31-56`) — the same data as a
   `Map<String, String>` with stringly-typed keys.

There is also a fourth, partial copy in `CallbackContext`'s `Update` constructor.

**Do:** delete both `@Deprecated` overloads (`InvolvedPartiesFactory.kt:67` and `:108`). Then decide
whether `InvolvedParties` / `MentionedPartyKey` / `toFlattenedMap()` are needed at all — after
T2.5.5, `NewUserInteractionHandler` is the only consumer, and it only wants a display string. If so,
replace the whole file with a `UserModel`/`ReplyModel` formatting function and delete the
`MentionedPartyKey` enum.

**Acceptance:** exactly one place in the codebase maps a Telegram message to `UserModel`/`ReplyModel`/`MessageModel`.

---

# Phase 3 — Remove the remaining `@Deprecated` and half-features

### [ ] T3.1 — Resolve the duplicate Telegram API executor (P1, S)

**Files:** `core/service/tgapi/executor/SpringTgApiCallExecutorImpl.kt`,
`core/service/tgapi/executor/KtorTgApiCallExecutorImpl.kt`, `core/config/ApplicationBeans.kt`

Two full implementations of `ITgApiCallExecutor` exist (132 lines each, ~80% duplicated retry and
error-classification logic). The Spring one is marked
`@Deprecated("just a fallback for now")` and carries a **broken conditional**:

```kotlin
@Deprecated("just a fallback for now")
@Service
@ConditionalOnMissingBean(ITgApiCallExecutor::class)
class SpringTgApiCallExecutorImpl(…)
```

`@ConditionalOnMissingBean` is only evaluated reliably on `@Bean` methods in auto-configuration; on a
component-scanned `@Service` it depends on registration order and is documented as unsupported. In
practice **both** beans are created and `KtorTgApiCallExecutorImpl` wins only because of `@Primary`.
The "fallback" therefore never falls back — it just holds a `RestTemplate` open.

**Do:** delete `SpringTgApiCallExecutorImpl.kt`. Then delete the now-unused `restTemplate()` bean
from `ApplicationBeans.kt:26-28`, and remove `@Primary` from `KtorTgApiCallExecutorImpl` since it is
the only implementation.

If the author wants a switchable engine (commit `d7c5038` suggests that was the intent), the correct
shape is a `@ConditionalOnProperty(name = "app.tg-api.engine", havingValue = "spring")` on a `@Bean`
method in a `@Configuration` class — not `@ConditionalOnMissingBean` on a `@Service`. Ask before
building that; the simpler answer is deletion.

**Acceptance:** `grep -rn '@Deprecated' src/main` no longer matches this file. Sending a message
still works.

### [ ] T3.2 — Decide the fate of `warehousebot/` (P1, S)

**Files:** `warehousebot/WarehouseBotCommandHandler.kt`, `warehousebot/WarehouseService.kt`,
`warehousebot/WarehouseConnector.kt`

The feature is dead code with live configuration: `handleUpdate` is `// TODO implement this` (empty),
the `commands` map is never invoked, and 13 lines of the old implementation sit commented out. Yet
`app.warehouse.url` is configured, documented in `AGENTS.md`, and points at a real service.

**This needs an author decision, not an agent guess.** Present both options:

- **Delete:** remove the package, `app.warehouse.*` from `application.yaml`, and the
  `APP_WAREHOUSE_URL` row from `AGENTS.md`.
- **Finish:** convert `/list`, `/get`, `/create`, `/update` into `@HandleCommand` methods on
  `CommandHandler` (dropping the hand-rolled `commands` map and the `UpdateHandler` interface
  entirely), using `UpdateContext`.

Do not port it to `UpdateContext` in T2.5.6 before this is decided.

### [ ] T3.3 — Decide the fate of the Discord integration (P1, S)

**Files:** `discord/DiscordService.kt`, `discord/DiscordDebugHandler.kt`, `core/api/InputController.kt:43-54`

The integration is a stub, not a feature. It can: verify an Ed25519 signature, answer Discord's
`type == 1` ping, and dump the raw JSON into Mongo. There is no command parsing, no dispatch, no
response payload (`InteractionResponse` has no data), no command registration, and the `{token}`
path variable is accepted without validation.

**Do:** either mark it explicitly as a spike (move to a `spike/` package excluded from scanning) or
finish it. Either way T6.1 must fix the fail-open signature check first, because the endpoint is
live in production today.

Also rename `discord/DiscordDebugHandler.kt` → `discord/InputJsonStorage.kt` so the filename matches
the class it contains.

---

# Phase 4 — Redesign the outgoing-message layer

### [ ] T4.1 — Introduce an engine-agnostic outgoing message (P1, M)

**Depends on:** T0.1

**File:** `core/service/tgapi/TgMessageService.kt`

The class carries its own design brief at line 17:

```
// TODO make message service non-bound to telegram, it should create a message-to-send entity and route it
//  to correct method executor
```

Current shape: **four** `sendMessage` overloads, **three** `editMessageText` overloads, and a
`sendDocument` with six positional parameters that has its own `// TODO this has multiple parameters,
use class as parameter` (line 128 — and an identical TODO exists at
`voicepad/VoicePadExecutionService.kt:66`, so this is a recognised pattern problem).

`editMessageText` also duplicates its body: the 5-arg version hand-builds a
`mutableMapOf<String, Any>` of snake_case keys and `valueToTree`s it, while `sendMessage` uses a
proper `TgSendMessage` class with `@JsonProperty`. Two serialisation styles in one class.

**Do:**

1. Introduce `TgEditMessage` and `TgSendDocument` alongside the existing `TgSendMessage`, each with
   `@JsonProperty` annotations and a `create { }` builder, mirroring `TgSendMessage.Companion.create`.
2. Reduce `TgMessageService` to one entry point per Bot API method, taking a parameter object.
3. Keep the convenience `replyToCurrentMessage(text)` / `sendMessage(chatId, text)` shorthands —
   they are used everywhere and are genuinely ergonomic. Delete the rest.
4. Drop the hand-built `mutableMapOf` + `valueToTree` path entirely.

Do **not** try to build the "route to correct method executor" abstraction in this task. There is
exactly one engine (Telegram) and one transport (Ktor, after T3.1). Premature abstraction is what
produced `BotInfo`/`BotType` (T4.2) and `ChildKeyboardProvider` (T1.6). Note the idea and stop.

**Acceptance:** `TgMessageService` has at most 5 public functions; no `mutableMapOf<String, Any>`
request bodies remain.

### [ ] T4.2 — Remove the `BotInfo` → `TgBotInfo` casts (P1, S)

**Depends on:** T2.3

The `BotInfo` / `BotType` abstraction has exactly one implementation (`TgBotInfo`) and one enum
constant (`BotType.TG`). It buys nothing and costs an unchecked downcast at every use site:

```kotlin
val tgBotInfo = getCurrentUpdateContext().getBotInfo() as? TgBotInfo
    ?: throw IllegalArgumentException("TgBotInfo is not an instance of TgBotInfo")
```

That exact block appears **three times** in `TgMessageService.kt` (lines 43-44, 103-104, 116-117),
once in `debug/TestCommandHandler.kt:116` (with `// TODO remove this cast`), and the same pattern in
`childcarebot/logic/ChildReportHelper`. The error message is also tautological.

**Do:** delete `BotInfo` and `BotType`; make `UpdateContext.getBotInfo()` return `TgBotInfo`. If a
second platform ever lands, reintroduce the interface then — with two implementations to design
against.

**Acceptance:** `grep -rn 'as? TgBotInfo' src/main` is empty. `core/entity/bots/BotInfo.kt` and
`BotType.kt` are gone.

### [ ] T4.3 — Unify the keyboard/markup types (P1, S)

**Depends on:** T1.1

After T1.1 there are two markup families left:

- `core/service/tgapi/TgMessagesDTO.kt` — `TgReplyMarkup`, `TgKeyboard`, `TgInlineKeyboard`,
  `TgInlineButton`, `TgRemoveKeyboard`, `TgButton`. **Used** for outgoing messages.
- `core/dto/InlineKeyboardMarkup.kt` + `core/dto/keyboard/InlineKeyboardButton.kt`. **Used** only to
  deserialise `Message.replyMarkup` on the way in.

That split is actually defensible (wire-in vs wire-out), but the naming hides it and
`core/dto/keyboard/InlineKeyboardButton.kt` carries a `// REWRITED THAT` comment plus a
mutable-`var`-with-nullable-`text` shape that no other DTO uses.

**Do:** keep both, but make the intent explicit: move the outgoing types into
`core/service/tgapi/dto/` and clean up `InlineKeyboardButton` into an immutable `data class` in line
with its neighbours. Delete the `// REWRITED THAT` comment. Also note the redundancy inside
`TgMessagesDTO.kt` itself: `TgInlineKeyboard.of(buttons)` and `TgSendMessage.withInlineKeyboard(buttons)`
build the identical structure from the identical input — keep one.

Also: `TgInlineButton : TgReplyMarkup` is wrong — a *button* is not a *markup*. Remove that
supertype.

**Acceptance:** `./gradlew build` passes; keyboards still render in Telegram (manual check).

---

# Phase 5 — Persistence layer

### [ ] T5.1 — Replace blocking `MongoTemplate` with a reactive/suspending API (P1, L)

**23 files** inject the blocking `MongoTemplate` and call it from `suspend` functions or from
`@Scheduled`/`@PostConstruct` methods, in a WebFlux application. `grep -rn 'ReactiveMongo' src/main`
returns nothing. Every Telegram update currently blocks an event-loop thread on Mongo I/O.

Affected (non-exhaustive, run `grep -rl MongoTemplate src/main` for the full list):

```
childcarebot/logic/ChildActivityRepo.kt      karmabot/service/UserService.kt
childcarebot/logic/ChildInfoRepo.kt          karmabot/service/actions/LikedHistoryService.kt
core/service/TgBotV2Service.kt               karmabot/service/actions/LikedMessageService.kt
core/service/helper/ErrorService.kt          karmabot/service/DynamicTextClassifier.kt
core/service/tgapi/TgLastKnownMessageService.kt   summary/SummaryMessageStorageService.kt
core/tooling/TracerService.kt                voicepad/VoicePadSessionService.kt
core/util/AppStorage.kt                      debug/UnparsedMessageService.kt
karmabot/commands/TopKarmaHandler.kt         debug/UnparsedMessagesCommandHandler.kt
karmabot/handlers/MessageStatHandler.kt      debug/interaction/UserInteractionService.kt
karmabot/service/EmojiService.kt             discord/DiscordDebugHandler.kt
                                             santabot/SantaBotCommandHandler.kt
```

**Do this incrementally, one package per commit.** Migrate to `ReactiveMongoTemplate` with the
`kotlinx-coroutines-reactor` `awaitSingle`/`awaitFirstOrNull` bridges (already a dependency), or to
coroutine `CoroutineCrudRepository`. Repository methods become `suspend`.

**Sequencing advice:** do `T5.2` first for the packages you touch — introducing the repository
interface and swapping the implementation behind it is much safer than changing 23 call sites in
place.

**Watch out:** `TgBotV2Service` is called from `TgRegisterUpdateFetchService`'s `@PostConstruct`
(which already wraps things in `runBlocking`). Bot lookup at start-up is legitimately blocking;
either keep a blocking path there or make registration an `ApplicationReadyEvent` listener.

### [ ] T5.2 — Introduce real repository boundaries (P1, M)

Classes named `*Repo` / `*Service` build ad-hoc `Query`/`Criteria` objects inline against
`MongoTemplate`, so there is no seam for tests and query logic leaks into handlers.

Worst offenders:

| File | Problem |
|---|---|
| `childcarebot/logic/ChildActivityRepo.kt` | `@Service` named `Repo`; inline `Query`; read-modify-save at lines 71-72 and 80-82 |
| `childcarebot/logic/ChildInfoRepo.kt` | same; read-mutate-save at 28-31 |
| `karmabot/commands/TopKarmaHandler.kt:26` | carries the TODO `do create a DAO for userInfo and karma already!` |
| `karmabot/service/actions/LikedHistoryService.kt:22-29` | builds `Criteria` by `reduce` — throws on an empty criteria list |

**Do:** for each domain aggregate, define an interface (`ChildActivityRepository`,
`ChildInfoRepository`, `UserInfoRepository`, `KarmaHistoryRepository`, `VoicePadSessionRepository`)
in the module's package, with an implementation in a `repository/` subpackage. Handlers depend on the
interface only. Resolves the `TopKarmaHandler:26` TODO.

**Acceptance:** no `@HandleCommand`-annotated class imports `MongoTemplate`, `Query` or `Criteria`.

### [ ] T5.3 — Fix atomicity and read-modify-write races (P1, M)

**Depends on:** T5.2

Concrete, individually-fixable data races:

1. `childcarebot/logic/ChildActivityRepo.kt:71-72` — `event.sentMessages += …; mongoTemplate.save(event)`
   with its own `// TODO update operation with push?`. Use `$push`.
2. `karmabot/service/actions/LikedMessageService.kt:30-38` — `userService.modifyUser(target)` is a
   read-then-save; two concurrent karma events lose one update. Use `findAndModify` / `$inc`.
3. `karmabot/handlers/MessageStatHandler.kt:41,58-65,116-120` — the entire day's stats for all chats
   live in one non-thread-safe `UserStat` with nested `mutableMapOf`, flushed by `@Scheduled` every
   10s. The class's own TODO (lines 25-34) documents this. Increment in Mongo with `$inc` on
   `date.chatId.userId`, or at minimum use `ConcurrentHashMap` with atomic counters.
4. `karmabot/service/DynamicTextClassifier.kt:20-21,49-68` — `defined`/`prefixes` are mutable maps
   loaded at `@PostConstruct`; `addEntry` updates them but `deleteEntry` **only deletes from Mongo**,
   so deletions never take effect until restart. Also unsynchronised against concurrent `classify`.
   Both methods carry `// TODO connect it to something` and have no caller — decide: wire them to an
   admin command (and fix the cache) or delete them.
5. `karmabot/service/actions/LikedHistoryService.kt:35-39` — `catch (e: Exception) { throw
   DuplicatedRatingError() }` around `insert` reports network and serialisation failures as duplicate
   ratings. Catch `DuplicateKeyException` only.
6. `core/service/tgapi/TgLastKnownMessageService.kt:23-31` — hand-rolled `ConcurrentHashMap` of lock
   objects + `synchronized`, then a blocking Mongo save, called from a coroutine. Replace with a
   conditional `$max` update and drop the locks entirely.

### [ ] T5.4 — Persist state that is currently in memory (P1, S)

**Depends on:** T5.2

| File | Lines | State | Consequence |
|---|---|---|---|
| `debug/log/LoggingConfigBackend.kt` | 7-12 | `mutableMapOf<Long, Boolean>()` | logging toggles reset on every deploy |
| `debug/interaction/NewUserInteractionHandler.kt` | 21 | `ConcurrentSet<String>()` | redundant with the `UserInteraction` Mongo collection it shadows; not cluster-safe |
| `core/service/tgapi/TgUpdatePollService.kt` | 35 | `ConcurrentSet<PollingInfo>()` | acceptable — polling state is genuinely per-instance |

**Do:** move `LoggingConfigBackend` to Mongo (`AppStorage` already exists for exactly this kind of
key/value state). Delete the `ConcurrentSet` cache in `NewUserInteractionHandler` and rely on
`UserInteractionService`, or replace it with `@Cacheable` — the app already has `@EnableCaching` and a
`ConcurrentMapCacheManager`, used by `ChildInfoRepo` and `SummaryService`.

### [ ] T5.5 — Fix unbounded and N+1 queries (P2, M)

1. `karmabot/commands/TopKarmaHandler.kt:27-29` — loads every non-zero-karma user then sorts and
   truncates in Kotlin. Push `.sort()` and `.limit()` into the query. Resolves the `// TODO add
   realtop` at line 22 as a side effect (decide what "realtop" means first).
2. `summary/SummaryService.kt:74-76` — loads *all* logged messages for a chat since a date with no
   limit. A busy group produces an unbounded prompt and an unbounded bill. Add a cap and a TTL index
   on the collection.
3. `core/tooling/TracerService.kt:33-38` — `removeOutdated()` is a `@Scheduled` method with the body
   `// nothing here so far`, and `list()` does `findAll().sortedByDescending { it.time }` in Kotlin.
   `app.tracer.ttl` and `app.tracer.capacity` are read into fields (lines 25-26) and **never used**.
   Either implement TTL/capacity enforcement (a Mongo TTL index is the right answer) or delete the
   config keys, the empty scheduled method and the `@PostConstruct createIndexes()` that only logs.

---

# Phase 6 — Correctness and security

### [ ] T6.1 — Make the Discord signature check fail closed (P0, S)

**File:** `discord/DiscordService.kt:39`

```kotlin
val publicKeyHex = appConfig.discord.publicKey?.takeIf { it.isNotBlank() } ?: return true
```

`DISCORD_PUBLIC_KEY` defaults to empty in `application.yaml:56`, so on any deployment where it is not
set, **every** unsigned payload to the public `POST /discord/{token}` endpoint is accepted and
written to Mongo. The `{token}` path variable is never validated either
(`core/api/InputController.kt:43-54`), so it provides no protection.

**Do:** return `false` when the key is missing. Additionally, make the whole Discord route
conditional on the key being configured (`@ConditionalOnProperty("app.discord.public-key")`) so the
endpoint does not exist when the feature is unconfigured. Validate the `token`.

**Acceptance:** with `DISCORD_PUBLIC_KEY` unset, `POST /discord/anything` returns 401 or 404 — not 200.

### [ ] T6.2 — Authenticate the debug handlers (P0, S)

`debug/` handlers are gated **only** by `requiredFeatures() = setOf(Features.DEBUG)`, which is a
per-bot flag in Mongo, not a per-user check. Any member of any chat served by a `debug`-enabled bot
can run them. Only `UnparsedMessagesCommandHandler` implements `Authenticable`.

Unprotected: `debug/MemStatusHandler` (leaks JVM memory layout), `debug/TestCommandHandler`,
`debug/VersionHandler`, `debug/log/ViewAllLoggedMessagesHandler`, `debug/log/LogAllMessagesHandler`.

`ViewAllLoggedMessagesHandler:24-59` is the sharpest edge: `/logging this on` is available to
anyone, and only the `admin all on` branch (line 46) checks `appConfig.adminId`. Turning it on makes
`LogAllMessagesHandler` echo full serialised updates back into the chat — a self-amplifying loop and
an information leak.

**Do:** make every `debug/` handler implement `Authenticable` against `TrustedUserService`
(`core/auth/TrustedUserService.kt` already exists and handles ids and `@usernames`). Consider a
shared `AdminOnly` marker interface that `UpdateProcessor` and `ChatCommandsHandler` honour, so this
cannot be forgotten again.

**Acceptance:** a non-trusted user gets no response from any `debug/` command.

### [ ] T6.3 — Fix child-care authorization and the broken `notFound` helper (P0, S)

**File:** `childcarebot/ChildParentsCommandHandler.kt`

1. **Missing authorization (lines 35-76):** `authenticate` only checks that the caller is a parent of
   *some* child; `/addparent <childId> <parentId>` and `/removeparent` never check that the caller is
   a parent of the **target** `childId`. Any parent can add themselves to any other family's child.
   Check the target.
2. **Unreachable success path (line 88):** `?: notFound(childId)` calls a generic helper that sends
   an error message and then always throws, so `listParents` can never return normally. Replace with
   an early `return` after sending the message.
3. `ChildCareCommandHandler.kt:54-66` and `90-101` duplicate the child-lookup block. Extract
   `getChildFor(userId)`.

### [ ] T6.4 — Fix time and timezone handling in `childcarebot` (P1, M)

The module stores `LocalDateTime` (no zone) and converts back and forth through a hand-rolled
`ChildTimezoneService`, which carries `// TODO This should become a cross-app service` (line 9).

Concrete bugs:

1. `childcarebot/logic/ChildTimezoneService.kt:39-40` — defaults are the strings `"UTC+2"` / `"UTC+1"`.
   **`ZoneId.of("UTC+2")` throws** — the valid forms are `UTC+02:00`, `+02:00` or `Etc/GMT-2`.
2. `childcarebot/ChildReplyHandler.kt:128-130` — `LocalTime.parse(text)` with the default formatter
   rejects single-digit hours, but `TIME_PATTERN` (line 157) accepts `1:15`, and
   `ChildReplyHandlerTest` only asserts that the **regex** matches `"1:15"` — so the test passes while
   the handler throws. The exception is then swallowed by a bare `catch (e: Exception)`.
3. `ChildReplyHandler.kt:89-95` vs `TIME_DIFF_PATTERN` (line 157) — the regex allows a bare `m`
   suffix that the `when` does not handle, giving `IllegalArgumentException` on `"5m"`.
4. `ChildReplyHandler.kt:119-126` — `changeEventTime` converts DB→UI, applies a zone-less
   `LocalTime`, converts back. Wrong across DST boundaries.
5. `childcarebot/Utils.kt:6-14` — `Duration.between` on two `LocalDateTime`s is only a physical
   duration if both are in the same zone; the DB/UI conversion path does not guarantee that.

**Do:** store `Instant` (or UTC `LocalDateTime` consistently, documented), convert to the user zone
only at the presentation boundary, compute durations on `Instant`. Validate/normalise zone ids at
config-parse time so a bad value fails at start-up, not at first report. Add tests for `1:15`,
`01:15`, `5m`, `5h` and a DST-crossing sleep interval.

### [ ] T6.5 — Real error handling on the update path (P1, M)

Four TODOs describe one missing design:

| File | Line | TODO |
|---|---|---|
| `core/service/MessageEntryPoint.kt` | 36 | `// TODO think about error handling` |
| `core/api/InputController.kt` | 39 | `// TODO return ok only if one of handlers/all supported handlers processed the message` |
| `core/handlers/ChatCallbackHandler.kt` | 39 | `// TODO maybe log all failed callbacks?` |
| `core/service/UpdateProcessor.kt` | 49-51 | `catch (e: Exception) { e.printStackTrace() }` |

Current behaviour: `MessageEntryPoint.proceedRawData` catches everything and logs; `InputController`
returns `200 OK` unconditionally, so Telegram never retries a genuinely failed update;
`UpdateProcessor.waitForJobCompletion` swallows every non-`ExpectedError` exception with
`printStackTrace()` (bypassing SLF4J entirely).

Also note `UpdateProcessor.proceedUpdate:26-28` throws `IllegalArgumentException("No handler found
…")` when nothing matches, which `InputController`'s `onError` maps to **HTTP 405** — an odd choice
for "nothing to do", and one that will make Telegram retry forever.

**Do:** define the contract explicitly. Suggested: an update with no matching handler is a success
(204/200, do not retry); a handler that throws is a partial failure (log with SLF4J at `error`,
report via `ErrorService`, still 200 so Telegram does not retry a poison message); a
deserialisation failure returns 200 and files an `UnparsedMessage` (this already works). Replace all
`printStackTrace()` in `src/main` with SLF4J — there are also instances in
`core/converters/DocumentToUpdateConverter.kt:31`, `debug/VersionProvider.kt:48` and
`summary/SummaryCommandHandler.kt:82`.

### [ ] T6.6 — Remove `runBlocking` from coroutine and reactive paths (P1, S)

| File | Lines | Problem |
|---|---|---|
| `debug/UnparsedMessageService.kt` | 18-20 | `runBlocking` in a synchronous `ApplicationListener`, with `// TODO fix that` |
| `karmabot/handlers/StickerReplyHandler.kt` | 69-80 | `runBlocking { launch { mongoTemplate.save(…) } }` **inside a `suspend fun`** |
| `karmabot/service/actions/LikedMessageService.kt` | 33-37 | `runBlocking { launch { … } }` inside a DB mutation callback |
| `core/service/tgapi/TgUpdatePollService.kt` | 69 | `runBlocking { jobs.joinAll() }` inside `@Scheduled` |
| `core/service/tgapi/TgRegisterUpdateFetchService.kt` | 38 | `runBlocking` in `@PostConstruct` |

The last two are defensible (a `@Scheduled`/`@PostConstruct` method is not a coroutine), but the
first three are not: `runBlocking` inside a `suspend` function blocks the carrier thread for no
reason, and the inner `launch` makes the write fire-and-forget-but-also-awaited.

**Do:** in `suspend` contexts call the `suspend` repository directly (after T5.1). For the
`ApplicationListener`, either use `@EventListener` on a `suspend` method or launch into the injected
`CoroutineScope` bean (`ApplicationBeans.coroutineScope`) — `VersionProvider` already does this
correctly and is a good template. For `TgUpdatePollService`/`TgRegisterUpdateFetchService`, migrate
to a coroutine `launch` on a long-lived scope and drop `@Scheduled`.

Also in scope: `MessageStatHandler.kt:77-83` — the `try` block's only statement is commented out
(line 79), so `reportInChat` silently never sends the daily statistics it computes. Decide: restore
the send (via `TgMessageService`, since `tgOperations` no longer exists) or delete the reporting path.
This resolves the `// TODO fix codestyle; refactor` at line 67.

### [~] T6.7 — Eliminate `!!` on remote-controlled data (P1, M) — partially done; mapper/callback/auth sites fixed, ~19 !! remain

**Depends on:** T2.1, T2.5.x

Telegram controls the shape of every incoming update, so `!!` on update-derived data is a
remotely-triggerable crash. Highest-risk sites:

| File | Lines |
|---|---|
| `core/service/TgUpdateContextMapper.kt` | 79-81 (three `!!` in one expression) |
| `core/handlers/callbacks/CallbackContext.kt` | 20-24 (four `!!` in a constructor) |
| `childcarebot/ChildReplyHandler.kt` | 34, 38, 41, 108, 148, 153 |
| `childcarebot/state/StateTransitionService.kt` | 45 |
| `karmabot/handlers/GroupChatKarmaHandler.kt` | 33, 46 |
| `karmabot/service/actions/LikedMessageService.kt` | 58 |
| `karmabot/service/actions/LikedHistoryService.kt` | 28 |
| `core/service/tgapi/TgLastKnownMessageService.kt` | 26 |
| all `authenticate` implementations | `update.getContextUserId()!!` — an anonymous sender crashes the handler instead of being rejected |

Most of these disappear as a side effect of T2.5.x if the migration is done properly (typed models
are nullable in the right places). Use this task as the verification sweep: after Phase 2, `grep -rn
'!!' src/main` should be short enough to review line by line, and every survivor should have a
justification.

---

# Phase 7 — Build, configuration and hygiene

### [~] T7.1 — Fix `build.gradle.kts` (P1, S) — partially done; kotlin-test scope fixed, toolchain/dep cleanup remains

**File:** `build.gradle.kts`

| Problem | Fix |
|---|---|
| `java.sourceCompatibility = JavaVersion.VERSION_17` and `jvmTarget.set(JvmTarget.JVM_17)` while CI uses JDK 21, the local toolchain is 21, and `AGENTS.md` claims JDK 21 | Move to `kotlin { jvmToolchain(21) }` + `JvmTarget.JVM_21`. Pick one source of truth. |
| `implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")` | Must be `testImplementation` — this currently ships a test framework inside the production jar |
| `implementation("org.springframework.boot:spring-boot-starter-validation")` | `grep -rn 'jakarta.validation\|@Valid\|@NotBlank' src/main` is empty. Remove it, or start using it for `AppConfig`. |
| `spring-boot-configuration-processor` declared as both `implementation` and `annotationProcessor` | Kotlin does not use `annotationProcessor` (needs kapt/KSP). Keep neither, or wire it properly. |
| `implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")` | Redundant — the Kotlin plugin adds the stdlib, and `jdk8` has been merged into the main artifact since 1.8 |
| unused `buildTime` / `versionName` vals | Remove (T1.7) |
| `spring-boot-starter-amqp` | Remove if T1.3's decision is "no AMQP" |

**Acceptance:** `./gradlew build` passes on JDK 21; `./gradlew dependencies --configuration runtimeClasspath` no longer lists `kotlin-test`.

### [ ] T7.2 — Make CI run the tests (P1, S)

**File:** `.github/workflows/github-build-push.yml`

The pipeline runs `./gradlew bootJar -PappVersion=…` — which does **not** run tests. Every test in
the repo could be failing and `master` would still deploy. `AGENTS.md` mandates `./gradlew build`;
CI does not honour its own project's rule.

**Do:** change the build step to `./gradlew build -PappVersion=…` (`build` depends on `test` and
`bootJar`). Add a `Publish test results` step so failures are visible.

**Acceptance:** a deliberately failing test blocks the pipeline.

### [~] T7.3 — Externalize prompts, models and user-facing strings (P2, M) — partially done; recap model externalized, prompts/strings still hardcoded

Model names and multi-line LLM prompts are compiled into class files, so tuning them requires a
deploy:

| File | Lines | Hardcoded |
|---|---|---|
| `summary/SummaryCommandHandler.kt` | 35 | `defaultModel = "google/gemma-4-31b-it"` — shadows `app.openRouter.defaultModel`, which already exists |
| `summary/SummaryService.kt` | 47, 80-101 | `maxTokens = 8000`, Russian `RECAP_SYSTEM_PROMPT` and user prompt template |
| `voicepad/VoicePadProcessor.kt` | 24-38, 42-71, 84-96, 100-131 | `CLAUDE_SONNET_MODEL = "anthropic/claude-sonnet-4-5"` plus two long system prompts |

User-facing text is scattered across handlers in two languages: Russian in
`childcarebot/logic/ChildStateTransitionProvider.kt:13-21`, `childcarebot/ChildCareCommandHandler.kt:72,75`,
`childcarebot/logic/ChildReportHelper.kt:28`, `santabot/SantaBotCommandHandler.kt`,
`voicepad/VoicePadExecutionService.kt:90-93`; English in `debug/TestCommandHandler.kt` and
`debug/UnparsedMessagesCommandHandler.kt`.

**Do:** move model names and `maxTokens` into `AppConfig` (`app.openRouter.*` already exists —
`SummaryCommandHandler` should read it, not shadow it). Move prompts into resource files loaded from
the classpath. Extract user-facing strings into per-module message constants or
`messages*.properties`. Note that `dictionary.yaml` is the karma word list and should not become a UI
string bundle.

**Acceptance:** changing the recap model requires only an env var.

### [ ] T7.4 — Make module layout consistent (P2, M)

Every module invented its own structure:

| Module | Layout |
|---|---|
| `core/` | `api`, `auth`, `config`, `converters`, `dto`, `entity`, `error`, `handlers`, `jobs`, `service`, `tooling`, `util` |
| `childcarebot/` | flat + `logic/` + `state/` |
| `karmabot/` | `commands/`, `handlers/`, `service/`, `service/actions/`, `service/classifier/` |
| `summary/` | flat + `ai/` + `entity/` |
| `debug/` | flat + `interaction/` + `log/` |
| `voicepad/`, `santabot/`, `warehousebot/`, `discord/`, `mq/` | flat |

Domain models are also embedded in handler files: `SecretSantaGame`/`SecretSantaPlayer` inside
`santabot/SantaBotCommandHandler.kt` (242 lines), `UserStat` + a top-level `getDateKey()` inside
`karmabot/handlers/MessageStatHandler.kt`, `AppData` inside `core/util/AppStorage.kt`,
`StickerReaction` inside `karmabot/handlers/StickerReplyHandler.kt`.

**Do:** agree one convention — suggested `<module>/{handler,service,repository,model}` — and apply
it. `karmabot` is closest; use it as the template. Split embedded domain models into `model/`.
Rename `childcarebot/logic/*Repo` to `repository/*Repository` as part of T5.2.

This is churn-heavy and conflicts with everything else, so **do it last**, after Phase 2 and 5 have
settled.

**Also in scope:** `core/util/` is a grab bag — `AppStorage` (a Mongo service), `MemoryTrackerService`
(a `@Service`), `UserFormatter`, `ChatCommandParser`, `JsonFlattenerService`, `InvolvedPartiesFactory`,
plus two files of extension functions (`Extended.kt`, `UpdateUtils.kt`). `Extended.kt` is a
meaningless name. Distribute these to real homes.

### [~] T7.5 — Fix and extend the test suite (P1, M) — partially done; new tests added, fake contextLoads and listed gaps remain

**Depends on:** T2.3, T2.4

Current state: 6 test files, ~150 lines total, for 9179 lines of production code. Of those:

1. `TGBotApplicationTests.kt:6` — `//@SpringBootTest` is commented out, so `contextLoads()` asserts
   nothing. The one test that would catch a broken bean graph is disabled. Enable it (with an
   embedded/mocked Mongo, or a slice test) or delete the file rather than keeping a fake green tick.
2. `handlers/ChatCommandTest.kt:63-75` — mocks `Update` with `RETURNS_DEEP_STUBS` and calls
   `chatCommandsHandler.handleUpdate(update)`. This will not compile after T2.3/T2.4; rewrite against
   `UpdateContext`. It is also the only test of the command-parsing path and only covers argument
   splitting.
3. `childcarebot/ChildReplyHandlerTest.kt` — asserts the regex matches `"1:15"` but never calls the
   parser, which is exactly why bug T6.4.2 is live. Test the parser, not the pattern.
4. `utils/CommandAnalyzerTest.kt` — the only real logic test (for `ChatCommandParser`).

**Do:** add tests for the pieces that are pure logic and currently untested:
`TgUpdateContextMapper` (feed it real Telegram JSON fixtures — `test/resources/responses/` already
has the pattern), `UpdateMarker` predicates, `TrustedUserService.parse`, `LikedMessageService.calculateKarmaDiff`,
`SummaryDateUtil`, `childcarebot/Utils.getDurationStringBetween`, and the time parsing from T6.4.

**Acceptance:** `./gradlew test` runs a meaningful suite; T7.2 makes CI enforce it.

### [x] T7.7 — Enable the JUnit Platform (P0, S) — DONE

**File:** `build.gradle.kts`

Found while establishing a green baseline for T0.1. `build.gradle.kts` never called
`useJUnitPlatform()`, so Gradle ran the **JUnit 4** runner, which does not discover
`org.junit.jupiter.api.Test`. Consequence: of 6 test classes, exactly **one** executed —
`ChildReplyHandlerTest`, and only because it is the single file using `kotlin.test.Test`, which
resolved to the JUnit 4 variant. `UtilsTest`, `CommandAnalyzerTest`, `TgResponseParserTest`,
`ChatCommandTest` and `TGBotApplicationTests` silently ran zero tests.

So T7.2 (CI runs only `bootJar`) was not the whole story — the suite did not run locally either. Any
"the tests pass" statement about this repo before this fix was meaningless.

**Done:** added `tasks.withType<Test> { useJUnitPlatform() }` and moved `kotlin-test` from
`implementation` to `testImplementation` (it was shipping in the production jar — the T7.1 item).
Executed tests: 1 → 23, all green.

Also fixed as part of this: `ChatCommandTest` did not compile at all — commit `c47ae2d` changed
`ChatCommandsHandler.handleUpdate` to take `UpdateContext` without updating the test, and the
main-source compile error from T0.1 was masking it. Rewritten to mock `UpdateContext`; a proper
rewrite is still T7.5.

### [ ] T7.6 — Small consistency fixes (P2, S)

Safe to batch into one pass, but keep it to exactly this list:

- **Logger declaration**: three styles coexist — `LoggerFactory.getLogger(this::class.java)`,
  `getLogger(this.javaClass)`, and a `companion object { private val logger = getLogger(this::class.java) }`
  in `CommandHandlerScanner.kt:39-41` (which is a **bug**: inside a companion, `this::class` is
  `Companion`, so the logger is named `CommandHandlerScanner$Companion`). Also the field name
  alternates between `logger` and `log`. Pick one form.
- **`ApplicationBeans.kt:47-57`** — the Ktor client re-configures Jackson inline with
  `// TODO use bean from above`, duplicating `objectMapper()`. Inject the bean.
- **`ApplicationBeans.kt:38`** — `Executors.newCachedThreadPool()` is an unbounded thread pool in a
  reactive app. Bound it, or use `Dispatchers.IO`.
- **`core/config/WebConfig.kt:12-18`** — CORS allows `*` for origins, methods and headers on a service
  whose only clients are Telegram webhooks and one token-protected endpoint. Restrict or delete.
- **`core/config/AppConfig.kt:18`** — `suspendBotRegistering` is declared as a body property while
  every other setting is a constructor parameter. Move it into the constructor for consistency.
- **`core/config/AppConfig.kt:20-40`** — nested config classes live inside a `companion object`,
  which is unusual and gains nothing. Make them plain nested classes.
- **`core/error/ExpectedErrors.kt:3`** — `open class ExpectedError : Exception()` never passes a
  message, so every log line about it is empty. Give it a `message` parameter.
- **`core/entity/CommonEntities.kt`** — a whole file for `typealias UserId = Long`, which is then
  ignored by most code (`Long` is used directly). Either use it consistently or delete it.
- **`.DS_Store`** files exist at `src/main/kotlin/com/nikichxp/tgbot/.DS_Store` and the repo root.
  They are untracked and `.gitignore` does not list them — add `.DS_Store` to `.gitignore`.
- **`debug/VersionHandler.kt:17`** — `// TODO add other commands, make it array`. Resolution: keep
  `@HandleCommand` single-valued and make it `@Repeatable` (Kotlin supports this) rather than
  changing the annotation's value to an array and touching every handler. Or just delete the TODO —
  there is no second version command.
- **`core/entity/MessageInteractionResult.kt:33`** — `// TODO ban, etc in the future`. Delete the
  TODO; a speculative enum comment is not a task.

---

# Phase 9 — Split into Gradle modules (one repo)

**Author's stated goal:** extract each bot (childcare, karma, santa, …) into its own module and import
them; eventually into separate repositories consuming a published `tg-bot-api` dependency.

Phase 9 is the in-repo half and is **worth doing**. Phase 10 is the separate-repos half and is
**probably not** — see the argument there.

## Why this is worth doing (and it is not "cleaner code")

The single concrete benefit: **the dependency direction becomes compiler-enforced.** Right now nothing
stops `core` from importing a feature package, and it does — measured:

```
core/jobs/UpdateEmojiJob.kt      -> karmabot (4 imports)
core/jobs/RecalculateKarmaJob.kt -> karmabot (5 imports)
core/api/InputController.kt      -> discord  (2 imports)
```

A package convention cannot prevent that. A Gradle module boundary can. That is the whole value
proposition; everything else (per-module tests, faster incremental builds, honest public surface) is
a bonus.

## Measured coupling — the split is much closer than it looks

| Metric | Count | Verdict |
|---|---|---|
| `core` → feature imports | 3 files | 2 die with T1.4; only `InputController → discord` is real |
| feature → feature imports | **1** (`voicepad → summary` for `LLMProvider`, `LLMRequest`) | one misplaced shared concern, T9.4 |
| feature → `core.dto` imports | 27, of which **23 are `Update`** | collapses to ~4 after T2.6 |
| feature → `core.handlers` | 66 | this *is* the SPI — the real API surface |
| feature → `core.entity` | 38 | `UpdateContext` + `entity/common/*Model` — also SPI |
| feature → `core.service` | 35 | mostly `TgMessageService` — also SPI |
| feature → `core.util` | 34 | grab bag; must shrink before it becomes public API (T7.4) |
| feature → `core.config` | 9 | `AppConfig` god-config, T9.3 |

The architecture is **already plugin-shaped**: `UpdateProcessor` injects `List<UpdateHandler>`,
`CommandHandlerScanner` injects `List<CommandHandler>`, and bot capabilities are open strings in Mongo
(`TgBotInfoV2Entity.supportedFeatures`). Spring collection injection works across modules unchanged.
Nothing in the runtime design needs to be invented — only the boundaries need to be drawn and held.

## The decisive sequencing argument

`core.dto` is 105 files. Feature modules import from it 27 times — and **23 of those are `Update`**.

- Split modules **before** T2.6 → `Update` is part of the feature API, so the shared artifact must
  export the entire 105-file hand-written Bot API mirror, and every DTO change becomes a breaking
  change for every module.
- Split **after** T2.6 → `core.dto` becomes internal to the platform module, invisible to features.

Do not skip ahead. Splitting now would freeze the two-formats mess into module boundaries, where it
becomes permanent.

## Target module graph

```
:app                     Spring Boot application. bootJar. Depends on everything. Contains no logic.
 │
 ├── :platform           Webhook intake, update routing, TG transport, bot registry, tracer, errors.
 │    └── :api           Handler SPI, UpdateContext + models, outgoing message DSL. No Mongo. No app.
 │
 ├── :ai                 LLMProvider / LLMRequest / OpenRouter client.
 │
 ├── :feature-childcare  ─┐
 ├── :feature-karma       │
 ├── :feature-summary     ├─ depend on :api (+ :ai where needed). Never on :platform.
 ├── :feature-voicepad    │  Never on each other.
 ├── :feature-santa       │
 └── :feature-debug      ─┘
```

**The one rule that makes this work:** a feature module depends on `:api`, never on `:platform` and
never on another feature. If a feature needs something from `:platform`, that thing belongs in `:api`
— or the feature is doing something it should not.

`:api` must not depend on Spring Boot, Mongo, or Ktor. It may depend on `spring-context` (for
`@Component`-style stereotypes) and `jackson-annotations`. If `:api` ends up needing
`spring-boot-starter-data-mongodb`, the line was drawn in the wrong place — see T9.6.

### [ ] T9.1 — Prerequisite gate (P2, S)

Do not start Phase 9 until all of these are ticked. Verify and record each one:

- [ ] **T2.6** — `getUpdate()` and the `Update` extensions are gone. Otherwise `core.dto` leaks into
      the public API of `:api`. Non-negotiable.
- [ ] **T4.2** — `BotInfo`/`BotType` deleted. Otherwise a bogus one-implementation abstraction gets
      baked into the published SPI.
- [ ] **T5.2** — repository interfaces exist per feature. Otherwise every feature needs
      `MongoTemplate` from `:api`, which forces Spring Data Mongo into the SPI and kills the thin-`:api`
      design.
- [ ] **T1.4** — `core/jobs/*` deleted, removing 9 of the 11 reverse dependencies.
- [ ] **T7.4** — package layout settled. Moving files across modules and restructuring packages in the
      same change is unreviewable; T7.4 is the dry run.
- [ ] **T7.1** — build file cleaned, JDK/toolchain consistent.

### [ ] T9.2 — Invert the feature registry (P1, S)

**File:** `core/handlers/Features.kt`

`core` currently enumerates every feature that exists:

```kotlin
object Features {
    const val CHILD_TRACKER = "childTracker"
    const val SANTA = "santa"
    const val KARMA = "karma"
    ...
}
```

That is core knowing about its own dependents — a reverse dependency in constant form. 18 files across
8 modules read it, all in the shape `requiredFeatures() = setOf(Features.X)`.

**Do:** delete `Features`. Each module declares its own id next to the handlers that use it, e.g.
`childcarebot/ChildCareFeature.kt` with `const val CHILD_TRACKER = "childTracker"`. **Keep the string
values byte-identical** — they are persisted in Mongo in `TgBotInfoV2Entity.supportedFeatures`, so
changing them silently disables bots.

Optionally add a `FeatureDescriptor` interface in `:api` that each module implements, so `:platform`
can collect the declared set for validation and a `/features` command — that is a *forward*
dependency and legal.

**Acceptance:** `grep -rn 'Features\.' src/main` is empty; every bot still responds to the same
commands as before.

### [ ] T9.3 — Split `AppConfig` (P1, M)

**File:** `core/config/AppConfig.kt`

One `@ConfigurationProperties(prefix = "app")` class holds every setting for every feature, and 8
non-core files reach into it:

| Reader | Reads |
|---|---|
| `summary/SummaryCommandHandler`, `summary/ai/OpenRouterLLMProvider`, `voicepad/VoiceTranscriptionService`, `voicepad/VoicePadCommandHandler` | `openRouter.*` |
| `karmabot/commands/RegisterEmojiHandler`, `debug/*` ×3 | `adminId` |
| `discord/DiscordService` | `discord.publicKey` |

So `:api` would have to export a config class that knows about OpenRouter, Discord and the warehouse —
i.e. the god-config becomes public API.

**Do:** give each module its own `@ConfigurationProperties`:

| New class | Prefix | Lives in |
|---|---|---|
| `PlatformConfig` (`webhook`, `localEnv`, `suspendBotRegistering`, `maxRetryCount`, `adminBot`) | `app` | `:platform` |
| `AdminConfig` (`adminId`, `trustedUsers`) | `app.admin` | `:api` or `:platform` — it backs `TrustedUserService`, which features legitimately need |
| `OpenRouterConfig` | `app.open-router` | `:ai` |
| `DiscordConfig` | `app.discord` | discord module |
| `TracerConfig` | `app.tracer` | `:platform` |

Keep the existing env-var names and YAML keys so no deployment changes. Also fix the two structural
oddities noted in T7.6 while here: `suspendBotRegistering` declared as a body property instead of a
constructor parameter, and the nested config classes hiding inside a `companion object`.

**Acceptance:** no feature module imports a config class owned by another feature. Application starts
with the unchanged `.env`.

### [ ] T9.4 — Extract the LLM abstraction into `:ai` (P1, S)

**Files:** `summary/ai/LLMProvider.kt`, `summary/ai/OpenRouterLLMProvider.kt`,
`voicepad/VoicePadProcessor.kt`, `voicepad/VoiceTranscriptionService.kt`

This is the **only** feature→feature dependency in the codebase:

```
voicepad/VoicePadProcessor.kt:3: import com.nikichxp.tgbot.summary.ai.LLMProvider
voicepad/VoicePadProcessor.kt:4: import com.nikichxp.tgbot.summary.ai.LLMRequest
```

`LLMProvider` is a general capability that happens to live inside the summary feature because summary
needed it first. `voicepad` reaching into `summary` is the exact coupling the module split must forbid.

**Do:** move `summary/ai/*` to its own top-level package (`ai/`, becoming `:ai`). `VoiceTranscriptionService`
also belongs there — it is an OpenRouter client with its own hand-rolled Ktor call, duplicating the
transport concern `OpenRouterLLMProvider` already owns. Consider merging them behind one client.

Combine with T7.3, which externalises the prompts and model names these classes hardcode — the prompts
are feature-specific and must stay with their features, while the client and config move to `:ai`.

**Acceptance:** `grep -rn 'import com.nikichxp.tgbot.summary' src/main/kotlin/com/nikichxp/tgbot/voicepad`
is empty. No feature package imports another feature package.

### [ ] T9.5 — Break `InputController` → `discord` (P1, S)

**Depends on:** T3.3

**File:** `core/api/InputController.kt:8-9,43-54`

The single remaining genuine reverse dependency: core's router hardcodes the Discord route, so
`:platform` would depend on a feature module. It also means every new transport requires editing core.

**Do:** let modules contribute their own routes. `coRouter` beans compose — `:platform` exposes the
Telegram + tracer routes, the discord module exposes its own `RouterFunction` bean, and Spring merges
them. Same pattern as the handler lists that already work.

If T3.3's decision is "delete Discord", this task disappears — do T3.3 first.

**Acceptance:** `grep -rn 'com.nikichxp.tgbot.discord' src/main/kotlin/com/nikichxp/tgbot/core` is empty.

### [ ] T9.6 — Draw the `:api` / `:platform` line (P1, M)

**Depends on:** T9.1

The most consequential design decision in Phase 9, and the one most likely to be got wrong. Decide
per class, then write the list into `AGENTS.md`.

Belongs in **`:api`** (what a feature author needs to write a bot):

- `handlers/{UpdateHandler, CommandHandler, CallbackHandler, Authenticable, BotSupportFeature}`
- `handlers/commands/{HandleCommand}` — the annotation, not the scanner
- `handlers/callbacks/CallbackContext`
- `entity/{UpdateContext, UpdateMarker}`, `entity/common/*Model`
- `entity/bots/TgBotInfo`
- outgoing message API: `TgMessageService` **interface**, `TgSendMessage`/`TgEditMessage` (after T4.1),
  the `Tg*` markup types (after T4.3)
- `error/ExpectedErrors`
- `auth/TrustedUserService` interface

Belongs in **`:platform`** (how updates get in and out — features must not see this):

- `api/InputController`, `config/{ApplicationBeans, WebConfig}`
- `service/{MessageEntryPoint, UpdateProcessor, TgUpdateContextMapper, TgBotV2Service}`
- `service/tgapi/**` implementations, `service/tgapi/executor/**`
- **all of `core/dto/**`** — this is the payoff from T2.6; features never see the Bot API mirror
- `converters/DocumentToUpdateConverter`, `entity/bots/TgBotInfoV2Entity`
- `handlers/{ChatCommandsHandler, ChatCallbackHandler}`, `handlers/commands/{CommandHandlerScanner, CommandHandlerExecutor, SingleCommandHandler}`
- `tooling/**`, `util/{JsonFlattenerService, Extended}`

**Needs an explicit decision** (do not let it default): `core/util/AppStorage` and
`core/service/helper/ErrorService`. Features use both, and both are thin `MongoTemplate` wrappers. If
they go in `:api`, `:api` gains a Mongo dependency and stops being a thin SPI. Prefer: expose
narrow interfaces (`KeyValueStore`, `ErrorReporter`) from `:api`, implement in `:platform`.

`core/util/MemoryTrackerService` is used only by `debug/MemStatusHandler` — move it into that module,
not into `:api`.

**Acceptance:** `:api`'s `build.gradle.kts` has no `spring-boot-starter-*`, no Mongo, no Ktor. If it
does, revisit the split.

### [ ] T9.7 — Perform the Gradle split (P2, M)

**Depends on:** T9.2, T9.3, T9.4, T9.5, T9.6

Mechanical once the above are done. Land it as **one commit that only moves files** — no behaviour
changes, no renames beyond the move — so the diff is reviewable as a move.

1. `settings.gradle.kts`: `include(":api", ":platform", ":ai", ":app", ":feature-childcare", …)`.
2. Introduce `gradle/libs.versions.toml`. Versions are currently hardcoded `val`s in
   `build.gradle.kts` (`ktorVersion`, `kotlinVersion`, `coroutinesVersion`) which cannot be shared
   across modules.
3. Move the Spring Boot BOM to a convention plugin in `buildSrc` (or a root `subprojects` block) so
   modules share dependency management without each re-declaring the plugin.
4. **Library modules must disable `bootJar` and enable `jar`** — `bootJar { enabled = false }` /
   `jar { enabled = true }`. Forgetting this produces unusable executable jars as dependencies and is
   the most common failure in this kind of split.
5. Only `:app` applies `org.springframework.boot` and keeps the `bootJar { archiveFileName.set("app.jar") }`
   + `Implementation-Version` manifest block that CI and `VersionProvider` depend on. Verify `/version`
   still works — the manifest now comes from a different module.
6. Move `application.yaml` to `:app`. Feature-specific defaults can live in each module's
   `META-INF/spring/…` or an additional profile-less YAML merged by Spring.

**Acceptance:** `./gradlew build` passes; `./gradlew :app:bootRun` behaves identically to today;
`app.jar` still runs. CI (T7.2) needs its `bootJar` task path updated to `:app:bootJar`.

### [ ] T9.8 — Per-module autoconfiguration instead of one big scan (P2, S)

**Depends on:** T9.7

`TGBotApplication` is `@SpringBootApplication` in `com.nikichxp.tgbot`, so a single component scan
picks up everything by package prefix. That keeps working after the split *only* if every module stays
under that prefix — a hidden constraint that will eventually be violated.

**Do:** give each feature module an `@AutoConfiguration` class registered in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Benefits beyond
tidiness:

- Modules become independently publishable without the app knowing their package names (Phase 10).
- `@ConditionalOnProperty` per module gives a free kill switch — disable a bot without deleting it or
  editing Mongo.
- Module beans stop leaking into unrelated `@SpringBootTest` slices, which currently makes testing
  anything require the whole graph.

**Trap to check while here:** `CommandHandlerScanner` resolves commands via
`handler::class.declaredFunctions` + `findAnnotation<HandleCommand>()`. If a handler bean ever becomes
a CGLIB proxy (`@Cacheable`, `@Transactional`, `@Async`, or some `@Conditional` arrangements), `::class`
returns the proxy class and `declaredFunctions` will not find the annotations — the command silently
disappears with no error. Autoconfiguration and conditional beans make proxying more likely. Use
`AopUtils.getTargetClass(handler)` (or `AopProxyUtils.ultimateTargetClass`) before reflecting. Worth
fixing regardless of Phase 9; it is a latent silent-failure bug today.

**Acceptance:** removing a feature module from `:app`'s dependencies removes its commands, with no
other edit.

### [ ] T9.9 — Enforce the boundaries mechanically (P2, S)

**Depends on:** T9.7

A boundary that is not enforced degrades — that is precisely how the current three reverse
dependencies appeared. Gradle already prevents cycles, but it will not stop `:feature-karma` from
depending on `:platform`.

**Do:**

1. Declare feature dependencies as `implementation(project(":api"))` — never `api(project(...))` — so
   transitive leakage does not compile.
2. Add a test (ArchUnit, or a Gradle task walking the configuration graph) asserting: no
   `:feature-*` depends on `:platform`; no `:feature-*` depends on another `:feature-*`; `:api`
   depends on neither.
3. Record the rule in `AGENTS.md` alongside the `:api`/`:platform` class list from T9.6, so the next
   agent does not have to re-derive it.

**Acceptance:** adding `implementation(project(":platform"))` to a feature module fails the build.

---

# Phase 10 — Separate repositories with a published `tg-bot-api`

**Author's stated goal.** Recorded here with an honest cost assessment, because this is the one part of
the plan I would push back on.

## Do not do this yet, and possibly not ever

Phase 9 delivers essentially all of the benefit. Phase 10 adds cost with, for this project, close to
zero additional benefit:

1. **Cross-cutting changes become multi-repo releases.** A change to the handler SPI — which Phases
   2–4 show happens regularly — currently touches one commit. After Phase 10: change `:api`, publish
   a version, bump and release each of ~6 feature repos, then bump the app. For a solo hobby project
   this converts an afternoon into a chore, and the predictable outcome is that the SPI stops
   improving because improving it is expensive. That is the opposite of the goal.
2. **You cannot atomically refactor across the boundary.** No IDE rename, no compiler-verified
   sweep, no single green build.
3. **There is one deployable, one JVM, one Mongo, one maintainer.** Separate repos buy independent
   release cadence and independent ownership. Neither applies.
4. **GitHub Packages is a poor fit specifically for Maven/Gradle.** Verified against GitHub's own
   docs: the Maven registry requires a **classic personal access token with `read:packages` even for
   public packages** — unlike the container registry, which allows anonymous pulls. So every consumer
   (including CI, including Dependabot, including a fresh clone on a new machine) needs a PAT
   configured before it can resolve dependencies. There is also no global index: each repository is
   its own Maven repo, so every consumer declares every producer's URL individually.
5. **Mongo stays shared.** The feature modules all write to the same database with no namespacing.
   Repository separation gives an illusion of isolation that the data layer does not honour — arguably
   worse than being honest about the coupling.

## Concrete trigger conditions

Revisit when **any one** of these becomes true — not before:

- A second person maintains one of the bots independently.
- A bot needs to be deployed separately (own scaling, own uptime requirement, own release cadence).
- A bot needs to run for someone else's Telegram bot / infrastructure.
- `:api` has been stable for months, i.e. the churn cost of Phase 10 has actually dropped.

Until then, Phase 9's `:feature-*` modules give the same conceptual separation with none of the
release overhead — and if a trigger does fire, a module is straightforward to lift into its own repo
*because* Phase 9 already forced the dependency direction to be correct. **Phase 9 is the prerequisite
that makes Phase 10 cheap; it is not a step toward it that must be completed.**

## If a trigger fires

Recorded so the decision does not have to be re-derived:

### [ ] T10.1 — Stabilise and version `:api` (P2, M)

Publishing means the SPI becomes a contract. Before that: settle the `Long` vs `String` id question
(T2.1), finish T4.1's message DSL, and adopt semantic versioning with a documented deprecation policy.
Note the tension with Appendix B's "zero `@Deprecated`" target — that target is correct *because* there
is no external consumer today. Publishing changes that, and deprecation cycles become mandatory. This
is a real, permanent cost of Phase 10.

### [ ] T10.2 — Choose the distribution channel (P2, S)

| Option | Consumer friction | Notes |
|---|---|---|
| GitHub Packages (Maven) | **PAT with `read:packages` required, even public** | Author's stated preference. Works, but every clone and every CI job needs credentials. |
| JitPack | none — builds from a git tag | Best fit for a public hobby project; no publish step, no auth. |
| Maven Central | none | Requires namespace verification and signing. Heavy for this. |
| Git submodule / Gradle composite build (`includeBuild`) | none | **Preserves atomic cross-repo refactoring.** Strictly better than publishing if the only goal is separate repos. |

Recommendation: if the motivation is repository separation rather than distribution, use a **Gradle
composite build** — separate repos, no publishing, no versioning ceremony, and the IDE still refactors
across the boundary. Publishing is only justified if a genuinely external consumer exists.

### [ ] T10.3 — Decide what the platform contract actually is (P2, M)

Unresolved question that determines whether Phase 10 is even coherent: is a feature repo a **library**
that a central `:app` assembles, or a **standalone deployable**?

- *Library* — the app repo still depends on all of them, so a feature change still requires an app
  release. Repository separation buys almost nothing over Phase 9's modules.
- *Standalone* — each feature needs the webhook intake, TG transport and bot registry, so `:platform`
  (not just `:api`) must be published, along with a shared-Mongo or multi-tenant story. That is a
  materially larger project than everything else in this document combined.

Answer this before writing any code. If the answer is "library", reread the pushback above.

---

# Appendix A — TODO inventory and disposition

Every `TODO`/`FIXME` in `src/main`, with the task that resolves it. Nothing should be left
unaccounted for.

| File:line | TODO | Resolved by |
|---|---|---|
| `core/service/tgapi/TgMessageService.kt:17` | make message service non-bound to telegram | T4.1 (documented as deliberately deferred) |
| `core/service/tgapi/TgMessageService.kt:128` | sendDocument has multiple parameters, use class | T4.1 |
| `core/entity/UpdateContext.kt:45` | do everything needed to remove `getUpdate()` | T2.6 |
| `core/dto/Update.kt:40` | maybe move `bot` to updateContext | T2.6 |
| `core/dto/ParseMode.kt:5` | remove `modeName` | T1.1 (file deleted) |
| `core/api/InputController.kt:39` | return ok only if handled | T6.5 |
| `core/service/MessageEntryPoint.kt:36` | think about error handling | T6.5 |
| `core/handlers/ChatCallbackHandler.kt:19` | do I need inline/callback features? | T2.3 (answer: no) |
| `core/handlers/ChatCallbackHandler.kt:39` | maybe log all failed callbacks | T6.5 |
| `core/config/ApplicationBeans.kt:50` | use objectMapper bean from above | T7.6 |
| `core/entity/MessageInteractionResult.kt:33` | ban, etc in the future | T7.6 (delete TODO) |
| `core/jobs/UpdateEmojiJob.kt:12` | move to executable script | T1.4 |
| `core/jobs/UpdateEmojiJob.kt:49` | `TODO()` in commented code | T1.4 |
| `debug/TestCommandHandler.kt:115` | remove this cast | T4.2 |
| `debug/VersionHandler.kt:17` | add other commands, make it array | T7.6 |
| `debug/UnparsedMessageService.kt:18` | fix `runBlocking` | T6.6 |
| `debug/UnparsedMessageService.kt:23` | reparse here as well | T6.6 (decide: one reparse entry point) |
| `childcarebot/ChildCareCallbackHandler.kt:37` | add filtering on command | T2.5.1 |
| `childcarebot/ChildKeyboardProvider.kt:41` | `TODO("Not yet implemented")` | T1.6 |
| `childcarebot/logic/ChildActivityRepo.kt:72` | update operation with push | T5.3 |
| `childcarebot/logic/ChildTimezoneService.kt:9` | should become cross-app service | T6.4 |
| `karmabot/handlers/MessageStatHandler.kt:26` | in-memory chats, LRU, multi-bot | T5.3 |
| `karmabot/handlers/MessageStatHandler.kt:67` | fix codestyle; refactor | T6.6 |
| `karmabot/handlers/StickerReplyHandler.kt:55` | fancy logger to tg chat + web ui | T6.5 (use `ErrorService`) |
| `karmabot/commands/RegisterEmojiHandler.kt:41` | refactor this | T2.5.2 |
| `karmabot/commands/TopKarmaHandler.kt:22` | add realtop | T5.5 |
| `karmabot/commands/TopKarmaHandler.kt:26` | create a DAO already | T5.2 |
| `karmabot/service/DynamicTextClassifier.kt:49` | connect it to something | T5.3 |
| `karmabot/service/DynamicTextClassifier.kt:57` | connect it to something | T5.3 |
| `karmabot/service/actions/LikedMessageService.kt:62` | errors in a properties file | T7.3 |
| `voicepad/VoicePadExecutionService.kt:39` | should probably be redesigned | T6.6 |
| `voicepad/VoicePadExecutionService.kt:66` | multiple parameters, use class | T4.1 |
| `warehousebot/WarehouseBotCommandHandler.kt:34` | implement this | T3.2 |
| `santabot/SantaBotCommandHandler.kt:32` | argsparser | T2.5.6 (`ChatCommandParser` already exists) |

# Appendix B — `@Deprecated` inventory

| File:line | Annotation | Resolved by |
|---|---|---|
| `core/entity/UpdateContext.kt:16` | `@Deprecated("for migration purposes only!")` on `getUpdate()` | T2.6 |
| `core/util/InvolvedPartiesFactory.kt:67` | `@Deprecated("Use createInvolvedParties with entities directly")` | T2.7 |
| `core/util/InvolvedPartiesFactory.kt:108` | same, `UpdateContext` overload | T2.7 |
| `core/service/tgapi/executor/SpringTgApiCallExecutorImpl.kt:26` | `@Deprecated("just a fallback for now")` | T3.1 |

Target state: **zero** `@Deprecated` in `src/main`. This is a single-consumer application with no
external API surface, so there is no reason to keep a deprecated symbol alive — deprecation here is
just a TODO with worse ergonomics.

# Appendix C — Duplicated-concept register

The "same thing in an old and a new format" list, which is the theme the author flagged. Each row is
a decision that was deferred; the plan forces it.

| Concept | Old | New | Task |
|---|---|---|---|
| Update model | `core/dto/Update` + `UpdateUtils` extensions | `UpdateContext` + `entity/common/*Model` | T2.x |
| "Mentioned message" resolution | `UpdateUtils.getMentionedMessage`, `TgUpdateContext.getMentionedMessage`, inline in `ChatUpdatesToPromptSerializerService` | one mapper | T2.2 |
| Party extraction | `InvolvedPartiesFactory(update)`, `getContextInvolvedParties()`, `CallbackContext(Update)` | `TgUpdateContextMapper` | T2.7 |
| Interaction result | `Update.convertToMessageIntResult()` | hand-built in `GroupChatKarmaHandler` | T2.5.2 |
| User display name | `core/util/UserFormatter` | `ChatUpdatesToPromptSerializerService.getAuthorNameFromMessage` | T2.5.3 |
| Telegram API transport | `SpringTgApiCallExecutorImpl` (RestTemplate) | `KtorTgApiCallExecutorImpl` | T3.1 |
| Reply markup | `core/dto/{InlineKeyboardMarkup,KeyboardReplyMarkup,ForceReplyMarkup,HideKeyboardReplyMarkup,ReplyKeyboardRemove}` | `core/service/tgapi/TgMessagesDTO.kt` `Tg*` | T1.1, T4.3 |
| Inline keyboard builder | `TgInlineKeyboard.of()` | `TgSendMessage.withInlineKeyboard()` | T4.3 |
| Payment DTOs | `core/dto/{SuccessfulPayment,OrderInfo,ShippingAddress}` | `core/dto/payments/*` | T1.2 |
| Message edit request | `mutableMapOf<String, Any>` + `valueToTree` | `TgSendMessage`-style class | T4.1 |
| Bot abstraction | `BotInfo` / `BotType` (1 impl, 1 constant) | `TgBotInfo` | T4.2 |
| Bot entity | `TgBotInfoV2Entity` (Mongo) | `TgBotInfo` (domain), with a copy-constructor | consider merging in T4.2; note the `V2` in the name is itself migration residue, as is `TgBotV2Service` |
| Keyboard provider | `ChildKeyboardProvider` abstract + `InlineKeyboardProvider` stub | `ReplyKeyboardProvider` (`@Primary`) | T1.6 |
| Karma stats storage | in-memory `UserStat` | should be Mongo `$inc` | T5.3 |
| Logging toggle storage | in-memory `LoggingConfigBackend` | should be `AppStorage` | T5.4 |
| New-user dedupe | in-memory `ConcurrentSet` | `UserInteractionService` (Mongo) | T5.4 |

---

# Suggested order

```
T0.1                                    ← unblocks everything
T1.1 T1.2 T1.3 T1.4 T1.5 T1.6 T1.7      ← independent, parallelisable, low risk
T7.1 T7.2                               ← do early so the rest is verified properly
T2.1 → T2.2 → T2.3 → T2.5.1…T2.5.6 → T2.4 → T2.6 → T2.7
T3.1 T3.2 T3.3                          ← T3.2/T3.3 are author decisions, ask early
T4.2 (after T2.3) → T4.1 → T4.3
T6.1 T6.2 T6.3                          ← security; can be pulled forward, they are small
T5.2 → T5.1 → T5.3 T5.4 T5.5
T6.4 T6.5 T6.6 T6.7
T7.3 T7.5 T7.6
T7.4                                    ← last of the cleanup: maximum conflict surface

T9.1                                    ← gate: verifies T2.6 T4.2 T5.2 T1.4 T7.4 T7.1 are done
T9.2 T9.3 T9.4 T9.5 → T9.6 → T9.7 → T9.8 → T9.9
T10.x                                   ← only if a trigger condition in Phase 10 fires
```

Three tasks need an author decision before an agent starts them: **T3.2** (warehouse: delete or
finish), **T3.3** (Discord: spike or finish), **T2.5.3** (`LoggedMessage` data migration strategy).
**T9.6** (the `:api` / `:platform` line) and **T10.3** (library vs standalone) are design decisions
that should not be delegated to an agent either.
