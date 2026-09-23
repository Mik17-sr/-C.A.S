package com.example.cas.ui.interview

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cas.ui.home.formatCaseDate
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInterviewScreen(
    caseId: Long,
    onInterviewCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NewInterviewViewModel = viewModel(factory = NewInterviewViewModel.Factory)
) {
    var person by remember { mutableStateOf("") }
    var findings by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var audioDurationSeconds by remember { mutableIntStateOf(0) }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is SaveInterviewState.Success) {
            onInterviewCreated()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva entrevista") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = person,
                onValueChange = { person = it },
                label = { Text("Nombre del entrevistado") },
                placeholder = { Text("Ej. Juan Pérez (Testigo)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = findings,
                onValueChange = { findings = it },
                label = { Text("Principales hallazgos") },
                placeholder = { Text("Ej. Confirmación de entrega de dinero sin soporte") },
                leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción / Notas de la entrevista") },
                placeholder = { Text("Detalles, contexto y observaciones de la conversación...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            DatePickerField(
                selectedDate = date,
                onDateSelected = { date = it }
            )

            AudioRecorderCard(
                onRecordingSaved = { path, seconds ->
                    audioPath = path
                    audioDurationSeconds = seconds
                }
            )

            if (saveState is SaveInterviewState.Error) {
                Text(
                    text = (saveState as SaveInterviewState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.createInterview(
                        caseId = caseId,
                        person = person,
                        findings = findings,
                        description = description,
                        date = date,
                        audioPath = audioPath,
                        audioDurationSeconds = audioDurationSeconds
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = saveState !is SaveInterviewState.Saving
            ) {
                if (saveState is SaveInterviewState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("GUARDAR ENTREVISTA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AudioRecorderCard(
    onRecordingSaved: (path: String, durationSeconds: Int) -> Unit
) {
    val context = LocalContext.current

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordTimeSeconds by remember { mutableIntStateOf(0) }
    var hasRecordedTrack by remember { mutableStateOf(false) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording(context) { recorder, path ->
                mediaRecorder = recorder
                recordedFilePath = path
                recordTimeSeconds = 0
                hasRecordedTrack = false
                isPlaying = false
                isRecording = true
            }
        }
    }

    // Libera los recursos nativos si la persona sale de la pantalla a mitad de una grabación o reproducción.
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.runCatching { stop(); release() }
            mediaPlayer?.runCatching { stop(); release() }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000L)
                recordTimeSeconds++
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PISTA DE AUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isRecording) {
                    Text(
                        text = "Grabando...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                } else if (hasRecordedTrack) {
                    Text(
                        text = "Audio grabado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (isRecording) {
                            mediaRecorder?.runCatching { stop(); release() }
                            mediaRecorder = null
                            isRecording = false
                            hasRecordedTrack = true
                            recordedFilePath?.let { onRecordingSaved(it, recordTimeSeconds) }
                        } else {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (granted) {
                                startRecording(context) { recorder, path ->
                                    mediaRecorder = recorder
                                    recordedFilePath = path
                                    recordTimeSeconds = 0
                                    hasRecordedTrack = false
                                    isPlaying = false
                                    isRecording = true
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Detener" else "Grabar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    val minutes = recordTimeSeconds / 60
                    val seconds = recordTimeSeconds % 60
                    val timeString = String.format(Locale.US, "%02d:%02d", minutes, seconds)

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when {
                            isRecording -> "Presiona detener al finalizar"
                            hasRecordedTrack -> "Pista de voz capturada con éxito"
                            else -> "Inicia grabación de audio de la entrevista"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasRecordedTrack && !isRecording && recordedFilePath != null) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                mediaPlayer?.runCatching { stop(); release() }
                                mediaPlayer = null
                                isPlaying = false
                            } else {
                                val player = MediaPlayer().apply {
                                    setDataSource(recordedFilePath)
                                    setOnCompletionListener {
                                        isPlaying = false
                                    }
                                    prepare()
                                    start()
                                }
                                mediaPlayer = player
                                isPlaying = true
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun startRecording(
    context: android.content.Context,
    onStarted: (recorder: MediaRecorder, path: String) -> Unit
) {
    val outputDir = File(context.filesDir, "audio_recordings").apply { mkdirs() }
    val outputFile = File(outputDir, "interview_${System.currentTimeMillis()}.m4a")

    @Suppress("DEPRECATION")
    val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }

    recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(outputFile.absolutePath)
        prepare()
        start()
    }

    onStarted(recorder, outputFile.absolutePath)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (selectedDate.isEmpty()) "" else formatCaseDate(selectedDate),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            placeholder = { Text("Elige una fecha") },
            trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millisToStoredDate(millis))
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun millisToStoredDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}