package com.rrat.demoworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintSet
import androidx.work.*
import com.rrat.demoworkmanager.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRequest.setOnClickListener {
            onClickOneTimeRequest()
        }

        binding.buttonPeriodic.setOnClickListener {
            onClickPeriodicRequest()
        }

    }

    private fun onClickOneTimeRequest()
    {
        //Configura el tipo de tarea y sus requerimientos
        val oneTimeRequestConstraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //Define la data a enviar a la Tarea
        val data = Data.Builder()
        data.putString("inputKey", "input value")

        //Configura la clase de la tarea y la data enviada
        val sampleWork = OneTimeWorkRequest
            .Builder(OneTimeRequestWorker::class.java)
            .setInputData(data.build())
            .setConstraints(oneTimeRequestConstraints)
            .build()

        //Inicia la tarea Async.
        WorkManager.getInstance(this).enqueue(sampleWork)

        //Recibe la respuesta de una Tarea en segundo Plano
        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(sampleWork.id)
            .observe(this, {
                    workInfo->
                OneTimeRequestWorker.Companion.logger(workInfo.state.name)
                if(workInfo!=null)
                    when(workInfo.state)
                    {
                        WorkInfo.State.ENQUEUED->{
                            binding.textRequest.text = "Task enqueue"
                        }
                        WorkInfo.State.BLOCKED->{
                            binding.textRequest.text = "Task blocked"
                        }
                        WorkInfo.State.RUNNING->{
                            binding.textRequest.text = "Task running"
                        }
                        else->{
                            binding.textRequest.text = "Task is a different state"
                        }
                    }
                if(workInfo != null && workInfo.state.isFinished)
                {
                    when(workInfo.state)
                    {
                        WorkInfo.State.SUCCEEDED->{
                            binding.textRequest.text = "Text successful"
                            //Get data output
                            val successOutputData = workInfo.outputData
                            val outputData = successOutputData.getString("outputKey")
                            Log.i("Worker Output", "$outputData")
                        }
                        WorkInfo.State.FAILED->{
                            binding.textRequest.text = "Task Failed"
                        }
                        WorkInfo.State.CANCELLED->{
                            binding.textRequest.text = "Task Cancelled"
                        }
                        else->{
                            binding.textRequest.text = "Task state isFinished else part."
                        }
                    }
                }
            })

    }

    private fun onClickPeriodicRequest()
    {
        //Configura el tipo de tarea y sus requerimientos
        val periodicRequestConstraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        //Configura la clase de la tarea y la data enviada
        val periodicWork = PeriodicWorkRequest
            .Builder(PeriodicRequestWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(periodicRequestConstraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("Periodic Work Request",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork)


    }
}