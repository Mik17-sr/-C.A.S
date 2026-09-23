package com.example.cas.ui.case

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.EvidenceEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.RecordEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.ui.components.StatusChip
import com.example.cas.ui.home.formatCaseDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    caseId: Long,
    onNavigateBack: () -> Unit,
    onAddInterview: (Long) -> Unit,
    viewModel: CaseDetailViewModel = viewModel(factory = CaseDetailViewModel.provideFactory(caseId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEvidenceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del caso") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        IconButton(onClick = { viewModel.toggleEdit() }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Cancel else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Cancelar edición" else "Editar"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar caso",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                is DetailUiState.Success -> {
                    val caseData = state.case
                    val interviews = state.interviews
                    val evidences = state.evidences
                    val records = state.records

                    if (isEditing) {
                        EditCaseForm(
                            case = caseData,
                            evidences = evidences,
                            onSave = { title, desc, date, status, conclusion ->
                                viewModel.updateCase(title, desc, date, status, conclusion)
                            },
                            onCancel = { viewModel.toggleEdit() },
                            onAddEvidenceClick = { showAddEvidenceDialog = true },
                            onDeleteEvidence = { viewModel.deleteEvidence(it) }
                        )
                    } else {
                        ViewCaseDetails(
                            case = caseData,
                            interviews = interviews,
                            evidences = evidences,
                            records = records,
                            saveStatus = saveStatus,
                            onAddInterview = { onAddInterview(caseData.case_id) },
                            onAddEvidenceClick = { showAddEvidenceDialog = true },
                            onDeleteEvidence = { viewModel.deleteEvidence(it) }
                        )
                    }
                }
                is DetailUiState.Deleted -> {
                    // Manejado en LaunchedEffect
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar este caso?") },
            text = { Text("Esta acción eliminará permanentemente el caso y todas sus evidencias y entrevistas asociadas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCase()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddEvidenceDialog) {
        AddEvidenceDialog(
            onDismiss = { showAddEvidenceDialog = false },
            onConfirm = { photoPath, description, date ->
                viewModel.addEvidence(photoPath, description, date)
                showAddEvidenceDialog = false
            }
        )
    }
}

@Composable
private fun ViewCaseDetails(
    case: CaseEntity,
    interviews: List<InterviewEntity>,
    evidences: List<EvidenceEntity>,
    records: List<RecordEntity>,
    saveStatus: String?,
    onAddInterview: () -> Unit,
    onAddEvidenceClick: () -> Unit,
    onDeleteEvidence: (EvidenceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = saveStatus != null) {
            if (saveStatus != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = saveStatus,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Text(
            text = case.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusChip(status = case.status)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatCaseDate(case.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "DESCRIPCIÓN DEL CASO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = case.description.ifEmpty { "Sin descripción disponible." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!case.conclusion.isNullOrEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CONCLUSIÓN / RESULTADOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = case.conclusion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVIDENCIAS DEL CASO (${evidences.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onAddEvidenceClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evidencia",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (evidences.isEmpty()) {
            Text(
                text = "Aún no se han adjuntado evidencias para este caso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            evidences.forEach { evidence ->
                EvidenceItemCard(
                    evidence = evidence,
                    onDelete = { onDeleteEvidence(evidence) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ENTREVISTAS REGISTRADAS (${interviews.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onAddInterview) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Añadir entrevista",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (interviews.isEmpty()) {
            Text(
                text = "No se han grabado entrevistas para este caso aún.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            interviews.forEach { interview ->
                InterviewItemCard(
                    interview = interview,
                    records = records.filter { it.interview_id == interview.interview_id }
                )
            }
        }
    }
}

@Composable
private fun EvidenceItemCard(
    evidence: EvidenceEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbnail by remember(evidence.photo) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(evidence.photo) {
        thumbnail = withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(evidence.photo)?.asImageBitmap() }.getOrNull()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = thumbnail
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidence.description.ifEmpty { "Evidencia fotográfica" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (evidence.date.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCaseDate(evidence.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar evidencia",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InterviewItemCard(
    interview: InterviewEntity,
    records: List<RecordEntity>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = interview.person.ifEmpty { "Entrevistado" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatCaseDate(interview.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = interview.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (interview.findings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hallazgos: ${interview.findings}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            records.forEach { record ->
                Spacer(modifier = Modifier.height(8.dp))
                AudioPlayButton(path = record.audio_path)
            }
        }
    }
}

@Composable
private fun AudioPlayButton(
    path: String?,
    modifier: Modifier = Modifier
) {
    if (path.isNullOrEmpty()) return

    var isPlaying by remember(path) { mutableStateOf(false) }
    var mediaPlayer by remember(path) { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(path) {
        onDispose {
            mediaPlayer?.runCatching { stop(); release() }
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                val current = mediaPlayer
                if (isPlaying && current != null) {
                    current.runCatching { stop(); release() }
                    mediaPlayer = null
                    isPlaying = false
                } else {
                    val player = MediaPlayer()
                    val started = runCatching {
                        player.setDataSource(path)
                        player.setOnCompletionListener { isPlaying = false }
                        player.prepare()
                        player.start()
                    }.isSuccess
                    if (started) {
                        mediaPlayer = player
                        isPlaying = true
                    }
                }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pausar" else "Reproducir grabación",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Escuchar grabación",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCaseForm(
    case: CaseEntity,
    evidences: List<EvidenceEntity>,
    onSave: (String, String, String, String, String?) -> Unit,
    onCancel: () -> Unit,
    onAddEvidenceClick: () -> Unit,
    onDeleteEvidence: (EvidenceEntity) -> Unit
) {
    var title by remember(case) { mutableStateOf(case.title) }
    var description by remember(case) { mutableStateOf(case.description) }
    var date by remember(case) { mutableStateOf(case.date) }
    var status by remember(case) { mutableStateOf(case.status) }
    var conclusion by remember(case) { mutableStateOf(case.conclusion ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar Caso",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del caso") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )

        DatePickerField(
            selectedDate = date,
            onDateSelected = { date = it }
        )

        StatusDropdown(
            selectedStatus = status,
            onStatusSelected = { status = it }
        )

        OutlinedTextField(
            value = conclusion,
            onValueChange = { conclusion = it },
            label = { Text("Conclusión (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVIDENCIAS Y ADJUNTOS (${evidences.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedButton(onClick = onAddEvidenceClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Subir foto")
            }
        }

        if (evidences.isNotEmpty()) {
            evidences.forEach { evidence ->
                EvidenceItemCard(
                    evidence = evidence,
                    onDelete = { onDeleteEvidence(evidence) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("CANCELAR")
            }

            Button(
                onClick = { onSave(title, description, date, status, conclusion) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("GUARDAR")
            }
        }
    }
}

private fun copyUriToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "evidence_photos").apply { mkdirs() }
        val outFile = File(dir, "evidence_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        outFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEvidenceDialog(
    onDismiss: () -> Unit,
    onConfirm: (photoPath: String, description: String, date: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoPath by remember { mutableStateOf<String?>(null) }
    var thumbnail by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCopying by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-09-22") }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isCopying = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    val path = copyUriToAppStorage(context, uri)
                    val bitmap = path?.let {
                        runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull()
                    }
                    path to bitmap
                }
                photoPath = result.first
                thumbnail = result.second
                isCopying = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjuntar evidencia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = {
                        pickPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = thumbnail
                        when {
                            isCopying -> CircularProgressIndicator()
                            bitmap != null -> Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca para elegir una foto",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción de la evidencia") },
                    placeholder = { Text("Ej. Recibo de consignación bancaria") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    selectedDate = date,
                    onDateSelected = { date = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { photoPath?.let { onConfirm(it, description, date) } },
                enabled = photoPath != null && !isCopying
            ) {
                Text("ADJUNTAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedStatus,
            onValueChange = {},
            readOnly = true,
            label = { Text("Estado") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CaseStatus.all.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onStatusSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun millisToStoredDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}