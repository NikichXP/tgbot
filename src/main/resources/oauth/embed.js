(function (window) {
  'use strict';

  var scriptSrc = document.currentScript && document.currentScript.src;
  var TGBOT_ORIGIN = scriptSrc ? new URL(scriptSrc).origin : null;

  function mount(container, options) {
    if (!TGBOT_ORIGIN) throw new Error('TgBotAuth: cannot detect tgbot origin, load embed.js via <script src>');
    if (!options || !options.clientId || !options.redirectUri) {
      throw new Error('TgBotAuth: clientId and redirectUri are required');
    }
    var element = typeof container === 'string' ? document.querySelector(container) : container;
    if (!element) throw new Error('TgBotAuth: container not found');

    var params = new URLSearchParams({
      client_id: options.clientId,
      redirect_uri: options.redirectUri,
      response_mode: 'web_message'
    });
    if (options.state != null) params.set('state', options.state);

    var iframe = document.createElement('iframe');
    iframe.src = TGBOT_ORIGIN + '/oauth/authorize?' + params.toString();
    iframe.title = 'Telegram login';
    iframe.style.border = '0';
    iframe.style.width = options.width || '100%';
    iframe.style.height = options.height || '110px';
    iframe.style.colorScheme = 'normal';
    iframe.setAttribute('allowtransparency', 'true');

    function onMessage(event) {
      if (event.origin !== TGBOT_ORIGIN || event.source !== iframe.contentWindow) return;
      var data = event.data;
      if (!data || data.type !== 'tgbot-oauth') return;
      if (options.state != null && data.state !== options.state) return;
      if (typeof options.onCode === 'function') options.onCode({ code: data.code, state: data.state });
    }

    window.addEventListener('message', onMessage);
    element.appendChild(iframe);

    return {
      iframe: iframe,
      destroy: function () {
        window.removeEventListener('message', onMessage);
        if (iframe.parentNode) iframe.parentNode.removeChild(iframe);
      }
    };
  }

  window.TgBotAuth = { mount: mount };
})(window);
