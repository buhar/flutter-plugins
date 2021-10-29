package cachet.plugins.health

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Intent
import android.os.Handler
import android.util.Log
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.SessionReadRequest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1111
const val CHANNEL_NAME = "flutter_health"

const val BODY_FAT_PERCENTAGE = "BODY_FAT_PERCENTAGE"
const val HEIGHT = "HEIGHT"
const val WEIGHT = "WEIGHT"
const val STEPS = "STEPS"
const val ACTIVE_ENERGY_BURNED = "ACTIVE_ENERGY_BURNED"
const val HEART_RATE = "HEART_RATE"
const val BODY_TEMPERATURE = "BODY_TEMPERATURE"
const val BLOOD_PRESSURE_SYSTOLIC = "BLOOD_PRESSURE_SYSTOLIC"
const val BLOOD_PRESSURE_DIASTOLIC = "BLOOD_PRESSURE_DIASTOLIC"
const val BLOOD_OXYGEN = "BLOOD_OXYGEN"
const val BLOOD_GLUCOSE = "BLOOD_GLUCOSE"
const val MOVE_MINUTES = "MOVE_MINUTES"
const val DISTANCE_DELTA = "DISTANCE_DELTA"

class HealthPlugin : MethodCallHandler, ActivityResultListener, Result, ActivityAware,
    FlutterPlugin {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    private var result: Result? = null
    private var handler: Handler? = null
    private var activity: Activity? = null

    private var BODY_FAT_PERCENTAGE = "BODY_FAT_PERCENTAGE"
    private var HEIGHT = "HEIGHT"
    private var WEIGHT = "WEIGHT"
    private var STEPS = "STEPS"
    private var ACTIVE_ENERGY_BURNED = "ACTIVE_ENERGY_BURNED"
    private var HEART_RATE = "HEART_RATE"
    private var BODY_TEMPERATURE = "BODY_TEMPERATURE"
    private var BLOOD_PRESSURE_SYSTOLIC = "BLOOD_PRESSURE_SYSTOLIC"
    private var BLOOD_PRESSURE_DIASTOLIC = "BLOOD_PRESSURE_DIASTOLIC"
    private var BLOOD_OXYGEN = "BLOOD_OXYGEN"
    private var BLOOD_GLUCOSE = "BLOOD_GLUCOSE"
    private var MOVE_MINUTES = "MOVE_MINUTES"
    private var DISTANCE_DELTA = "DISTANCE_DELTA"
    private var WATER = "WATER"
    private var SLEEP_ASLEEP = "SLEEP_ASLEEP"
    private var SLEEP_AWAKE = "SLEEP_AWAKE"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME);
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}

    /// DataTypes to register
    // private val fitnessOptions = FitnessOptions.builder()
    //         .addDataType(keyToHealthDataType(BODY_FAT_PERCENTAGE), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(HEIGHT), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(WEIGHT), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(STEPS), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(ACTIVE_ENERGY_BURNED), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(HEART_RATE), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(BODY_TEMPERATURE), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(BLOOD_PRESSURE_SYSTOLIC), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(BLOOD_OXYGEN), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(BLOOD_GLUCOSE), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(MOVE_MINUTES), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(DISTANCE_DELTA), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(WATER), FitnessOptions.ACCESS_READ)
    //         .addDataType(keyToHealthDataType(SLEEP_ASLEEP), FitnessOptions.ACCESS_READ)
    //         .accessActivitySessions(FitnessOptions.ACCESS_READ)
    //         .accessSleepSessions(FitnessOptions.ACCESS_READ)
    //         .build()


    override fun success(p0: Any?) {
        handler?.post { result?.success(p0) }
    }

    override fun notImplemented() {
        handler?.post { result?.notImplemented() }
    }

    override fun error(
        errorCode: String, errorMessage: String?, errorDetails: Any?
    ) {
        handler?.post { result?.error(errorCode, errorMessage, errorDetails) }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("FLUTTER_HEALTH", "Access Granted!")
                mResult?.success(true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("FLUTTER_HEALTH", "Access Denied!")
                mResult?.success(false)
            }
        }
        return false
    }

    private var mResult: Result? = null

    private fun keyToHealthDataType(type: String): DataType {
        return when (type) {
            BODY_FAT_PERCENTAGE -> DataType.TYPE_BODY_FAT_PERCENTAGE
            HEIGHT -> DataType.TYPE_HEIGHT
            WEIGHT -> DataType.TYPE_WEIGHT
            STEPS -> DataType.TYPE_STEP_COUNT_DELTA
            ACTIVE_ENERGY_BURNED -> DataType.TYPE_CALORIES_EXPENDED
            HEART_RATE -> DataType.TYPE_HEART_RATE_BPM
            BODY_TEMPERATURE -> HealthDataTypes.TYPE_BODY_TEMPERATURE
            BLOOD_PRESSURE_SYSTOLIC -> HealthDataTypes.TYPE_BLOOD_PRESSURE
            BLOOD_PRESSURE_DIASTOLIC -> HealthDataTypes.TYPE_BLOOD_PRESSURE
            BLOOD_OXYGEN -> HealthDataTypes.TYPE_OXYGEN_SATURATION
            BLOOD_GLUCOSE -> HealthDataTypes.TYPE_BLOOD_GLUCOSE
            MOVE_MINUTES -> DataType.TYPE_MOVE_MINUTES
            DISTANCE_DELTA -> DataType.TYPE_DISTANCE_DELTA
            WATER -> DataType.TYPE_HYDRATION
            SLEEP_ASLEEP -> DataType.TYPE_SLEEP_SEGMENT
            SLEEP_AWAKE -> DataType.TYPE_SLEEP_SEGMENT
            else -> DataType.TYPE_STEP_COUNT_DELTA
        }
    }

    private fun getUnit(type: String): Field {
        return when (type) {
            BODY_FAT_PERCENTAGE -> Field.FIELD_PERCENTAGE
            HEIGHT -> Field.FIELD_HEIGHT
            WEIGHT -> Field.FIELD_WEIGHT
            STEPS -> Field.FIELD_STEPS
            ACTIVE_ENERGY_BURNED -> Field.FIELD_CALORIES
            HEART_RATE -> Field.FIELD_BPM
            BODY_TEMPERATURE -> HealthFields.FIELD_BODY_TEMPERATURE
            BLOOD_PRESSURE_SYSTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC
            BLOOD_PRESSURE_DIASTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC
            BLOOD_OXYGEN -> HealthFields.FIELD_OXYGEN_SATURATION
            BLOOD_GLUCOSE -> HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
            MOVE_MINUTES -> Field.FIELD_DURATION
            DISTANCE_DELTA -> Field.FIELD_DISTANCE
            WATER -> Field.FIELD_VOLUME
            SLEEP_ASLEEP -> Field.FIELD_SLEEP_SEGMENT_TYPE
            SLEEP_AWAKE -> Field.FIELD_SLEEP_SEGMENT_TYPE
            else -> Field.FIELD_PERCENTAGE
        }
    }

    /// Extracts the (numeric) value from a Health Data Point
    private fun getHealthDataValue(dataPoint: DataPoint, unit: Field): Any {
        return try {
            dataPoint.getValue(unit).asFloat()
        } catch (e1: Exception) {
            try {
                dataPoint.getValue(unit).asInt()
            } catch (e2: Exception) {
                try {
                    dataPoint.getValue(unit).asString()
                } catch (e3: Exception) {
                    Log.e("FLUTTER_HEALTH::ERROR", e3.toString())
                }
            }
        }
    }

    private fun getData(call: MethodCall, result: Result) {
        if (activity == null) {
            result.success(null)
            return
        }

        val type = call.argument<String>("dataTypeKey")!!
        val startTime = call.argument<Long>("startDate")!!
        val endTime = call.argument<Long>("endDate")!!

        // Look up data type and unit for the type key
        val dataType = keyToHealthDataType(type)
        val unit = getUnit(type)

        /// Start a new thread for doing a GoogleFit data lookup
        thread {
            try {
                val typesBuilder = FitnessOptions.builder()
                typesBuilder.addDataType(dataType)
                if (dataType == DataType.TYPE_SLEEP_SEGMENT) {
                    typesBuilder.accessSleepSessions(FitnessOptions.ACCESS_READ)
                }
                val fitnessOptions = typesBuilder.build()
                val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity!!.applicationContext, fitnessOptions)

                if (dataType != DataType.TYPE_SLEEP_SEGMENT) {
                    val response = Fitness.getHistoryClient(activity!!.applicationContext, googleSignInAccount)
                        .readData(DataReadRequest.Builder()
                            .read(dataType)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build())

                    /// Fetch all data points for the specified DataType
                    val dataPoints = Tasks.await<DataReadResponse>(response).getDataSet(dataType)

                    /// For each data point, extract the contents and send them to Flutter, along with date and unit.
                    val healthData = dataPoints.dataPoints.mapIndexed { _, dataPoint ->
                        return@mapIndexed hashMapOf(
                                "value" to getHealthDataValue(dataPoint, unit),
                                "date_from" to dataPoint.getStartTime(TimeUnit.MILLISECONDS),
                                "date_to" to dataPoint.getEndTime(TimeUnit.MILLISECONDS),
                                "unit" to unit.toString(),
                                "source_name" to (dataPoint.getOriginalDataSource().appPackageName ?: (dataPoint.originalDataSource?.getDevice()?.model ?: "" )),
                                "source_id" to dataPoint.getOriginalDataSource().getStreamIdentifier()
                        )
                    }

                    activity!!.runOnUiThread { result.success(healthData) }
                } else {
                    // request to the sessions for sleep data
                    val request = SessionReadRequest.Builder()
                            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                            .enableServerQueries()
                            .readSessionsFromAllApps()
                            .includeSleepSessions()
                            .build()
                    Fitness.getSessionsClient(activity!!.applicationContext, googleSignInAccount)
                            .readSession(request)
                            .addOnSuccessListener { response ->
                                var healthData: MutableList<Map<String, Any?>> = mutableListOf()
                                for (session in response.sessions) {

                                    // Return sleep time in Minutes if requested ASLEEP data
                                    if (type == SLEEP_ASLEEP) {
                                        healthData.add(
                                                hashMapOf(
                                                        "value" to session.getEndTime(TimeUnit.MINUTES) - session.getStartTime(TimeUnit.MINUTES),
                                                        "date_from" to session.getStartTime(TimeUnit.MILLISECONDS),
                                                        "date_to" to session.getEndTime(TimeUnit.MILLISECONDS),
                                                        "unit" to "MINUTES",
                                                        "source_name" to session.appPackageName,
                                                        "source_id" to session.identifier
                                                )
                                        )
                                    }

                                    // If the sleep session has finer granularity sub-components, extract them:
                                    if (type == SLEEP_AWAKE) {
                                        val dataSets = response.getDataSet(session)
                                        for (dataSet in dataSets) {
                                            for (dataPoint in dataSet.dataPoints) {
                                                // searching SLEEP AWAKE data
                                                if (dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt() == 1) {
                                                    healthData.add(
                                                            hashMapOf(
                                                                    "value" to dataPoint.getEndTime(TimeUnit.MINUTES) - dataPoint.getStartTime(TimeUnit.MINUTES),
                                                                    "date_from" to dataPoint.getStartTime(TimeUnit.MILLISECONDS),
                                                                    "date_to" to dataPoint.getEndTime(TimeUnit.MILLISECONDS),
                                                                    "unit" to "MINUTES",
                                                                    "source_name" to (dataPoint.originalDataSource.appPackageName
                                                                            ?: (dataPoint.originalDataSource.device?.model
                                                                                    ?: "unknown")),
                                                                    "source_id" to dataPoint.originalDataSource.streamIdentifier
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                activity!!.runOnUiThread { result.success(healthData) }
                            }
                            .addOnFailureListener { exception ->
                                activity!!.runOnUiThread { result.success(null) }
                                Log.i("FLUTTER_HEALTH::ERROR", exception.message ?: "unknown error")
                                Log.i("FLUTTER_HEALTH::ERROR", exception.stackTrace.toString())
                            }
                }
            } catch (e3: Exception) {
                activity!!.runOnUiThread { result.success(null) }
            }
        }
    }

    private fun callToHealthTypes(call: MethodCall): FitnessOptions {
        val typesBuilder = FitnessOptions.builder()
        val args = call.arguments as HashMap<*, *>
        val types = args["types"] as ArrayList<*>
        for (typeKey in types) {
            if (typeKey !is String) continue
            typesBuilder.addDataType(keyToHealthDataType(typeKey), FitnessOptions.ACCESS_READ)
            if (typeKey == SLEEP_ASLEEP || typeKey == SLEEP_AWAKE) {
                typesBuilder.accessSleepSessions(FitnessOptions.ACCESS_READ)
            }
        }
        return typesBuilder.build()
    }

    private fun hasOAuthPermission(fitnessOptions: FitnessOptions): Boolean {
        return GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(activity),
            fitnessOptions
        )
    }

    /// Called when the "hasPermissions" is invoked from Flutter
    private fun hasPermissions(call: MethodCall, result: Result) {
        val options = callToHealthTypes(call)

        if (hasOAuthPermission(options)) {
            result.success(true)
        } else {
            result.success(false)
        }
    }

    /// Called when the "requestAuthorization" is invoked from Flutter
    private fun requestAuthorization(call: MethodCall, result: Result) {
        val options = callToHealthTypes(call)
        mResult = result

        val isGranted = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), optionsToRegister)

        /// Not granted? Ask for permission
        if (!isGranted && activity != null) {
            GoogleSignIn.requestPermissions(
                    activity!!,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    optionsToRegister)
        }

        if (hasOAuthPermission(fitnessOptions)) {
            mResult?.success(true)
            return
        }

        GoogleSignIn.requestPermissions(
            activity!!,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            GoogleSignIn.getLastSignedInAccount(activity),
            options
        )
    }

    /// Handle calls from the MethodChannel
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "hasPermissions" -> hasPermissions(call, result)
            "requestAuthorization" -> requestAuthorization(call, result)
            "getData" -> getData(call, result)
            else -> result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        binding.addActivityResultListener(this)
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        channel.setMethodCallHandler(null)
    }
}