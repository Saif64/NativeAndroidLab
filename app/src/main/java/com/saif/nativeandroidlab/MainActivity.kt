package com.saif.nativeandroidlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.nativeandroidlab.ui.theme.NativeAndroidLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NativeAndroidLabTheme {
                DeveloperCard(
                    name = "Muntasir Mahmud Saif",
                    experienceYears = 4,
                )
            }
        }
    }
}

/**
 * Day 5 reference checkpoint.
 *
 * State stays inside the smallest composable that needs it. Day 7 will earn state
 * hoisting, and Day 10 will earn a ViewModel.
 */
@Composable
fun DeveloperCard(
    name: String,
    experienceYears: Int,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            DayHeader()
            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.lab_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.lab_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))
            SectionLabel(stringResource(R.string.execution_trace))
            Spacer(Modifier.height(16.dp))
            ExecutionTrace()

            NotebookDivider()
            LearnerSummary(name = name, experienceYears = experienceYears)

            Spacer(Modifier.height(28.dp))
            SectionLabel(stringResource(R.string.live_experiment))
            Spacer(Modifier.height(12.dp))
            PracticeExperiment()

            Spacer(Modifier.height(20.dp))
            ReadingPrompt()
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DayHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.day_stamp),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text = stringResource(R.string.day_progress),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ExecutionTrace(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        TraceStep(
            number = "01",
            title = stringResource(R.string.trace_state_write_title),
            explanation = stringResource(R.string.trace_state_write_body),
        )
        TraceConnector()
        TraceStep(
            number = "02",
            title = stringResource(R.string.trace_snapshot_title),
            explanation = stringResource(R.string.trace_snapshot_body),
        )
        TraceConnector()
        TraceStep(
            number = "03",
            title = stringResource(R.string.trace_recomposition_title),
            explanation = stringResource(R.string.trace_recomposition_body),
        )
    }
}

@Composable
private fun TraceStep(
    number: String,
    title: String,
    explanation: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TraceConnector() {
    Box(
        Modifier
            .padding(start = 19.dp, top = 4.dp, bottom = 4.dp)
            .width(2.dp)
            .height(18.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun LearnerSummary(
    name: String,
    experienceYears: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.learner_context))
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.flutter_experience,
                        experienceYears,
                        experienceYears,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.learning_native),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.learning_path),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PracticeExperiment(
    modifier: Modifier = Modifier,
) {
    var practiceSessions by rememberSaveable { mutableIntStateOf(0) }
    val counterDescription = stringResource(
        R.string.practice_sessions_count_description,
        practiceSessions,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = counterDescription
                    liveRegion = LiveRegionMode.Polite
                },
            ) {
                Text(
                    text = stringResource(R.string.practice_sessions),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = practiceSessions.toString().padStart(length = 2, padChar = '0'),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { practiceSessions += 1 },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.complete_session))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { practiceSessions = 0 },
                enabled = practiceSessions > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.reset))
            }
        }
    }
}

@Composable
private fun ReadingPrompt(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp)) {
            SectionLabel(stringResource(R.string.try_next))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.try_next_body),
                style = MaterialTheme.typography.bodyLarge,
            )

            NotebookDivider()
            SectionLabel(stringResource(R.string.flutter_bridge_label))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.flutter_bridge_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            NotebookDivider()
            SectionLabel(stringResource(R.string.read_code_label))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.read_code_path),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.primary,
            )

            NotebookDivider()
            SectionLabel(stringResource(R.string.exit_gate_label))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.exit_gate_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun NotebookDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview(
    name = "Day 5 reference",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun DeveloperCardPreview() {
    NativeAndroidLabTheme {
        DeveloperCard(
            name = "Muntasir Mahmud Saif",
            experienceYears = 4,
        )
    }
}
