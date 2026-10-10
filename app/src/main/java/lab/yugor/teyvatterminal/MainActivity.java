package lab.yugor.teyvatterminal;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;

import com.google.mlkit.common.model.DownloadConditions;
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
        webView.addJavascriptInterface(new AndroidBridge(this, webView),"AndroidBridge");
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
    public void translateTest(String text) {
        if (text == null || text.trim().isEmpty()) {
            sendError("No text provided");
            return;
        }

        TranslatorOptions options =
            new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.RUSSIAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();

        Translator translator =
            Translation.getClient(options);

        DownloadConditions conditions =
            new DownloadConditions.Builder()
                .build();

        translator
            .downloadModelIfNeeded(conditions)
            .addOnSuccessListener(unused -> {
                translator
                    .translate(text)
                    .addOnSuccessListener(result -> {
                        sendResult(result);
                        translator.close();
                    })
                    .addOnFailureListener(error -> {
                        sendError(error.getMessage());
                        translator.close();
                    });
            })
            .addOnFailureListener(error -> {
                sendError(error.getMessage());
                translator.close();
            });
    }

    private void sendResult(String result) {
        String safeResult =
            JSONObject.quote(result == null ? "" : result);

        activity.runOnUiThread(() ->
            webView.evaluateJavascript(
                "window.onMlKitTranslationResult(" +
                safeResult +
                ");",
                null
            )
        );
    }

    private void sendError(String message) {
        String safeMessage =
            JSONObject.quote(
                message == null
                    ? "Unknown ML Kit error"
                    : message
            );

        activity.runOnUiThread(() ->
            webView.evaluateJavascript(
                "window.onMlKitTranslationError(" +
                safeMessage +
                ");",
                null
            )
        );
    }
}
