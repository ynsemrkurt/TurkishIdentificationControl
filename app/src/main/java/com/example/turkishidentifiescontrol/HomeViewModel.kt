package com.example.turkishidentifiescontrol

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.time.LocalDate
import javax.xml.parsers.DocumentBuilderFactory

class HomeViewModel : ViewModel() {

    private val _verificationResult = MutableLiveData<@receiver:StringRes Int>()
    val verificationResult: LiveData<Int> get() = _verificationResult

    fun verifyIdentity(name: String, surname: String, identityNumber: String, birthYear: String) {
        if (validateInputs(name, surname, identityNumber, birthYear)) {
            val soapRequestBody = createSoapRequestBody(name, surname, identityNumber, birthYear)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.verifyIdentity(soapRequestBody)
                    if (response.isSuccessful && parseSoapResponse(response.body())) {
                        _verificationResult.postValue(R.string.verification_successful)
                    } else {
                        _verificationResult.postValue(R.string.validation_error)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _verificationResult.postValue(R.string.validation_error)
                }
            }
        }
    }

    private fun validateInputs(
        name: String,
        surname: String,
        identityNumber: String,
        birthYear: String
    ): Boolean {
        if (name.isEmpty() || surname.isEmpty() || identityNumber.isEmpty() || birthYear.isEmpty()) {
            _verificationResult.postValue(R.string.please_fill_in_all_fields)
            return false
        }
        if (identityNumber.length != 11) {
            _verificationResult.postValue(R.string.id_number_must_be_11_digits)
            return false
        }
        val currentYear = LocalDate.now().year
        if (birthYear.length != 4 || birthYear.toInt() < 1900 || birthYear.toInt() > currentYear) {
            _verificationResult.postValue(R.string.please_enter_a_valid_year_of_birth)
            return false
        }
        return true
    }

    private fun createSoapRequestBody(
        name: String,
        surname: String,
        identityNumber: String,
        birthYear: String
    ): RequestBody {
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap12:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
              <soap12:Body>
                <TCKimlikNoDogrula xmlns="http://tckimlik.nvi.gov.tr/WS">
                  <TCKimlikNo>$identityNumber</TCKimlikNo>
                  <Ad>$name</Ad>
                  <Soyad>$surname</Soyad>
                  <DogumYili>$birthYear</DogumYili>
                </TCKimlikNoDogrula>
              </soap12:Body>
            </soap12:Envelope>
        """.trimIndent()
        return soapBody.toRequestBody("application/soap+xml; charset=utf-8".toMediaType())
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