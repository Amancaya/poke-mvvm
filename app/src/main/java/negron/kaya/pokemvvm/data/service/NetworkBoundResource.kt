package negron.kaya.pokemvvm.data.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*
import negron.kaya.pokemvvm.utils.DataState
import negron.kaya.pokemvvm.utils.errorLogs

abstract class NetworkBoundResource<ResponseObject, RequestType>(val methodName: String, isNetworkAvailable: Boolean) {

    private val TAG: String = "NetworkBoundResource"
    protected val result = MediatorLiveData<DataState<ResponseObject>>()
    protected lateinit var job: CompletableJob
    private lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initJob())
        setValue(DataState.loading(isLoading = true))
        if (isNetworkAvailable) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    val apiResponse = createCall()
                    result.addSource(apiResponse) { response ->
                        result.removeSource(apiResponse)
                        coroutineScope.launch {
                            handleResponse(response)
                        }
                    }
                }
            }

            GlobalScope.launch(Dispatchers.IO) {
//                delay(ServiceApi.NETWORK_TIMEOUT)
                if (!job.complete() && job.isCancelled.not()) {
                    Log.e(TAG, "JOB NETWORK TIMEOUT.")
                    onNetworkError("NetworkBoundResource: JOB NETWORK TIMEOUT.")
                }
            }

        } else {
            coroutineScope.launch {
                Log.e(TAG, "JOB NETWORK TIMEOUT.")
                onNetworkError("NetworkBoundResource: JOB NETWORK TIMEOUT.")
            }
        }
    }

    private suspend fun handleResponse(response: GenericApiResponse<RequestType>) {
        when(response) {
            is ApiSuccessResponse -> { handleApiSuccessResponse(response) }
            is ApiErrorResponse -> {
                response.errorMessage.errorLogs(tag = TAG)
                onReturnError(response.errorMessage)
            }
            is ApiEmptyResponse -> { }
            is ApiNotFoundResponse -> { onReturnNotFound() }
        }
    }

    private fun onCompleteJob(dataState: DataState<ResponseObject>){
        Log.e(TAG, dataState.toString())
        GlobalScope.launch(Dispatchers.Main) {
            job.complete()
            setValue(dataState)
        }
    }

    @UseExperimental(InternalCoroutinesApi::class)
    private fun initJob(): Job {
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object: CompletionHandler{
            override fun invoke(cause: Throwable?) {
                if(job.isCancelled){
                    Log.e(TAG, "NetworkBoundResource: Job has been cancelled.")
                    cause?.let{
                        it.message?.errorLogs(tag = TAG)
                        onReturnError(it.message.toString())
                    }?: onReturnError("Unknown error.")
                }
                else if(job.isCompleted){
                    result.value?.isNetworkError?.let{
                        //&& result.value?.message == null && it
                        if (result.value?.data == null) {
                            onNetworkError("NetworkBoundResource: JOB NETWORK TIMEOUT.")
                        }
                    }
                    Log.e(TAG, "Job has been completed.")
                }
            }
        })

        coroutineScope = CoroutineScope(Dispatchers.IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ResponseObject>>

    fun setValue(dataState: DataState<ResponseObject>) { result.value = dataState }

    fun onReturnError(message: String) = onCompleteJob(DataState.error(message))

    fun onNetworkError(message: String) = onCompleteJob(DataState.networkError(message))

    fun onReturnNotFound() = onCompleteJob(DataState.error("Not Found"))

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RequestType>)

    abstract fun createCall(): LiveData<GenericApiResponse<RequestType>>

    abstract fun setJob(job: Job)
}