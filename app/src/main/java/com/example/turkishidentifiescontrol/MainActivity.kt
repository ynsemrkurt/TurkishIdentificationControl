package com.example.turkishidentifiescontrol

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val buttonControl = findViewById<AppCompatButton>(R.id.buttonControl)
        buttonControl.setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.textName).text.toString()
            val surname = findViewById<TextInputEditText>(R.id.textSurName).text.toString()
            val identityNumber = findViewById<TextInputEditText>(R.id.textIdentityNumber).text.toString()
            val birthYear = findViewById<TextInputEditText>(R.id.textBirthYear).text.toString()

            val soapRequestTask = SoapRequestTask()
            soapRequestTask.execute(name, surname, identityNumber, birthYear)
        }
    }

    inner class SoapRequestTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean {
            val client = OkHttpClient()

            val mediaType = "application/soap+xml; charset=utf-8".toMediaType()
            val requestBody = getSoapRequestBody(params[0], params[1], params[2], params[3])
            val request = Request.Builder()
                .url("https://tckimlik.nvi.gov.tr/Service/KPSPublic.asmx")
                .post(requestBody)
                .addHeader("Content-Type", "application/soap+xml; charset=utf-8")
                .addHeader(
                    "Cookie",
                    "TS01326bb0=0179b2ce456757ef584bd54018d03ef6bde1c4dd044111d4a52c7617577d052b47928daeb9c9509323b952a0b2cfb2ab4746016de3"
                )
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                return parseSoapResponse(responseBody)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                showToast("Doğrulama Başarılı")
            } else {
                showToast("Doğrulama Hatalı")
            }
        }

        private fun getSoapRequestBody(name: String?, surname: String?, identityNumber: String?, birthYear: String?): okhttp3.RequestBody {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">\r\n  <soap12:Body>\r\n    <TCKimlikNoDogrula xmlns=\"http://tckimlik.nvi.gov.tr/WS\">\r\n      <TCKimlikNo>$identityNumber</TCKimlikNo>\r\n      <Ad>$name</Ad>\r\n      <Soyad>$surname</Soyad>\r\n      <DogumYili>$birthYear</DogumYili>\r\n    </TCKimlikNoDogrula>\r\n  </soap12:Body>\r\n</soap12:Envelope>".toRequestBody(
                "application/soap+xml; charset=utf-8".toMediaType()
            )
        }

        private fun parseSoapResponse(responseBody: String?): Boolean {
            try {
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                val input = ByteArrayInputStream(responseBody?.toByteArray())

                val document: Document = builder.parse(input)
                val element: Element = document.documentElement

                val nodeList: NodeList = element.getElementsByTagName("TCKimlikNoDogrulaResult")

                if (nodeList.length > 0) {
                    val resultValue = nodeList.item(0).textContent.trim()
                    return resultValue.toBoolean()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}