// Site settings for the web client. server.js serves its own version of this
// file built from environment variables; edit this copy only if you host web/
// with a different web server.
window.MOPE_CONFIG = {
    // OAuth client ID from Google Cloud Console ("Web application" type).
    // Leave empty to hide the Google login button.
    googleClientId: "",
    // reCAPTCHA v3 site key; only needed when the game server's ENABLE_CAPTCHA is on.
    recaptchaSiteKey: "",
    // Google AdSense publisher ID ("ca-pub-..."). Leave empty for no ads.
    adsenseClient: "",
    // AdSense ad unit IDs (numbers): the 300x250 menu rectangle and the 728x90 bottom banner.
    adsenseSlotRect: "",
    adsenseSlotBanner: "",
    // Account server address. Empty means this page's host on port 14002.
    accountServerUrl: ""
};
