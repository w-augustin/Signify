package com.example.signifybasic.features.tabs.dictionary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.signifybasic.R
import android.widget.TextView
import com.example.signifybasic.features.tabs.HomePage

data class SignVideo(
    val url: String,
    val start: Int? = null,
    val end: Int? = null
)


class DictionaryFragment : Fragment() {

    // Create a dictionary of sign names mapped to video URLs (these could be local or remote)
    private val signDictionary = mapOf(
        "hello" to SignVideo("https://www.youtube.com/embed/SsLvqfTXo78"),
        "hi" to SignVideo("https://www.youtube.com/embed/SsLvqfTXo78"),
        "thank you" to SignVideo("https://www.youtube.com/embed/2W0BDFUjsG0"),
        "thanks" to SignVideo("https://www.youtube.com/embed/2W0BDFUjsG0"),
        "book" to SignVideo("https://www.youtube.com/embed/SUlKivg4SDg"),
        "name" to SignVideo("https://www.youtube.com/embed/GbeC9TFuSX4"),
        "i love you" to SignVideo("https://www.youtube.com/embed/jzJjdvTF10A"),
        "a" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 3, end = 6),
        "b" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 7, end = 10),
        "c" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 11, end = 14),
        "d" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 15, end = 18),
        "e" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 19, end = 22),
        "f" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 23, end = 25),
        "g" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 26, end = 29),
        "h" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 30, end = 33),
        "i" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 33, end = 35),
        "j" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 36, end = 38),
        "k" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 39, end = 42),
        "l" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 43, end = 45),
        "m" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 46, end = 48),
        "n" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 49, end = 52),
        "o" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 53, end = 55),
        "p" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 56, end = 58),
        "q" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 59, end = 61),
        "r" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 62, end = 65),
        "s" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 67, end = 69),
        "t" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 70, end = 72),
        "u" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 73, end = 75),
        "v" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 76, end = 77),
        "w" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 78, end = 81),
        "x" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 82, end = 84),
        "y" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 85, end = 87),
        "z" to SignVideo("https://www.youtube.com/embed/tkMg8g8vVUo", start = 88, end = 91)
    )


    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var videoWebView: WebView
    private lateinit var errorMessage: TextView
    private lateinit var backButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_dictionary, container, false)

        // Initialize views
        searchInput = rootView.findViewById(R.id.searchInput)
        searchButton = rootView.findViewById(R.id.searchButton)
        videoWebView = rootView.findViewById(R.id.videoWebView)
        errorMessage = rootView.findViewById(R.id.errorMessage)
        backButton = rootView.findViewById(R.id.backToHomeButton)

        videoWebView.settings.javaScriptEnabled = true
        videoWebView.settings.loadWithOverviewMode = true
        videoWebView.settings.useWideViewPort = true
        videoWebView.webViewClient = WebViewClient()

        // Set up the search button click listener
        searchButton.setOnClickListener {
            val searchText = searchInput.text.toString().trim().lowercase()
            val signVideo = signDictionary[searchText]

            if (signVideo != null) {
                val urlWithParams = buildString {
                    append(signVideo.url)
                    if (signVideo.start != null) append("?start=${signVideo.start}")
                    if (signVideo.end != null) {
                        if (signVideo.start == null) append("?")
                        else append("&")
                        append("end=${signVideo.end}")
                    }
                    append("&autoplay=1")
                }

                val html = """
                    <html>
                        <body style="margin:0">
                            <iframe width="100%" height="100%" 
                                src="$urlWithParams" 
                                frameborder="0" allow="autoplay; encrypted-media" 
                                allowfullscreen></iframe>
                        </body>
                    </html>
                """
                videoWebView.loadData(html, "text/html", "utf-8")
                videoWebView.visibility = View.VISIBLE
                errorMessage.visibility = View.GONE
            } else {
                videoWebView.visibility = View.GONE
                errorMessage.visibility = View.VISIBLE
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(activity, HomePage::class.java)  // Navigate to HomePage activity
            startActivity(intent)
            activity?.finish()
        }

        return rootView
    }
}
