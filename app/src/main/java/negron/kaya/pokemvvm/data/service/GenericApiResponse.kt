package negron.kaya.pokemvvm.data.service

import com.google.gson.Gson
import retrofit2.Response
import negron.kaya.pokemvvm.models.Error

sealed class GenericApiResponse<T> {

    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(
                error.message ?: "unknown error"
            )
        }

        fun <T> create(response: Response<T>): GenericApiResponse<T> {
            return if(response.isSuccessful){
                val body = response.body()
                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                }
                else {
                    ApiSuccessResponse(
                        body = body
                    )
                }
            } else{
                if (response.code() == 404 && response.raw().toString().contains("clients")) {
                    ApiNotFoundResponse()
                } else {

                    val msg: Error = Gson().fromJson(response.errorBody()?.string(), Error::class.java)

                    ApiErrorResponse(
                        msg.error
                    )
                }
            }
        }
    }
}

class ApiNotFoundResponse<T> : GenericApiResponse<T>()

class ApiEmptyResponse<T> : GenericApiResponse<T>()

data class ApiSuccessResponse<T>(val body: T) : GenericApiResponse<T>()

data class ApiErrorResponse<T>(val errorMessage: String) : GenericApiResponse<T>()