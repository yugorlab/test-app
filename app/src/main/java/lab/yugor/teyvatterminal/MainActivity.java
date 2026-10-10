package lab.yugor.teyvatterminal;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF050607);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(
            new AndroidBridge(this, webView),
            "AndroidBridge"
        );
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
        }
        super.onDestroy();
    }

    public static class AndroidBridge {
        private final Activity activity;
        private final WebView webView;

        AndroidBridge(Activity activity, WebView webView) {
            this.activity = activity;
            this.webView = webView;
        }

        @JavascriptInterface
        public void copyText(String text) {
            ClipboardManager clipboard =
                (ClipboardManager) activity.getSystemService(
                    Context.CLIPBOARD_SERVICE
                );

            if (clipboard != null) {
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "Teyvat Terminal",
                        text == null ? "" : text
                    )
                );
            }
        }

        @JavascriptInterface
        public String mlKitStatus() {
            return "ML Kit bridge ready";
        }

        @JavascriptInterface
        public void translate(
            String text,
            String source,
            String target,
            int requestId
        ) {
            if (text == null || text.trim().isEmpty()) {
                sendError("No text provided", requestId);
                return;
            }

            if (target == null || target.trim().isEmpty()) {
                sendError("Target language is missing", requestId);
                return;
            }

            if ("auto".equals(source)) {
                detectAndTranslate(text, target, requestId);
            } else {
                startTranslation(text, source, target, requestId);
            }
        }

        private void detectAndTranslate(
            String text,
            String target,
            int requestId
        ) {
            LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();

            languageIdentifier
                .identifyLanguage(text)
                .addOnSuccessListener(languageCode -> {
                    languageIdentifier.close();

                    if ("und".equals(languageCode)) {
                        sendError(
                            "Language could not be detected",
                            requestId
                        );
                        return;
                    }

                    startTranslation(
                        text,
                        languageCode,
                        target,
                        requestId
                    );
                })
                .addOnFailureListener(error -> {
                    languageIdentifier.close();
                    sendError(error.getMessage(), requestId);
                });
        }

        private void startTranslation(
            String text,
            String source,
            String target,
            int requestId
        ) {
            String sourceLanguage =
                TranslateLanguage.fromLanguageTag(source);

            String targetLanguage =
                TranslateLanguage.fromLanguageTag(target);

            if (sourceLanguage == null) {
                sendError(
                    "Unsupported source language: " + source,
                    requestId
                );
                return;
            }

            if (targetLanguage == null) {
                sendError(
                    "Unsupported target language: " + target,
                    requestId
                );
                return;
            }

            if (sourceLanguage.equals(targetLanguage)) {
                sendResult(text, requestId);
                return;
            }

            TranslatorOptions options =
                new TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build();

            Translator translator = Translation.getClient(options);

            DownloadConditions conditions =
                new DownloadConditions.Builder().build();

            translator
                .downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused ->
                    translator
                        .translate(text)
                        .addOnSuccessListener(result -> {
                            sendResult(result, requestId);
                            translator.close();
                        })
                        .addOnFailureListener(error -> {
                            sendError(error.getMessage(), requestId);
                            translator.close();
                        })
                )
                .addOnFailureListener(error -> {
                    sendError(error.getMessage(), requestId);
                    translator.close();
                });
        }

        private void sendResult(String result, int requestId) {
            String safeResult =
                JSONObject.quote(result == null ? "" : result);

            activity.runOnUiThread(() ->
                webView.evaluateJavascript(
                    "window.onMlKitTranslationResult(" +
                    safeResult + "," + requestId + ");",
                    null
                )
            );
        }

        private void sendError(String message, int requestId) {
            String safeMessage =
                JSONObject.quote(
                    message == null
                        ? "Unknown ML Kit error"
                        : message
                );

            activity.runOnUiThread(() ->
                webView.evaluateJavascript(
                    "window.onMlKitTranslationError(" +
                    safeMessage + "," + requestId + ");",
                    null
                )
            );
        }
    }
}
