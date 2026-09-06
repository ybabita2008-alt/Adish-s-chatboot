package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

data class PromptItem(
    val category: String,
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val targetMode: AiMode
)

val SamplePrompts = listOf(
    PromptItem(
        category = "Coding",
        title = "Build REST API",
        prompt = "Write a clean Python function to fetch data from an API and handle errors gracefully using requests.",
        icon = Icons.Default.Terminal,
        targetMode = AiMode.UNIFIED
    ),
    PromptItem(
        category = "Algorithms",
        title = "Optimize & Explain",
        prompt = "Explain Dijkstra's shortest path algorithm with time complexity analysis and provide a clean implementation in Kotlin.",
        icon = Icons.Default.Code,
        targetMode = AiMode.UNIFIED
    ),
    PromptItem(
        category = "Debugging",
        title = "Troubleshoot Bug",
        prompt = "Why does this JavaScript code output undefined? How do I fix asynchronous Promise resolution in loops?",
        icon = Icons.Default.BugReport,
        targetMode = AiMode.UNIFIED
    ),
    PromptItem(
        category = "Database",
        title = "Complex SQL Query",
        prompt = "Write an advanced SQL query using window functions (ROW_NUMBER and LAG) to calculate month-over-month customer churn.",
        icon = Icons.Default.Storage,
        targetMode = AiMode.UNIFIED
    ),
    PromptItem(
        category = "Concept",
        title = "Explain Concept",
        prompt = "Explain how transformer neural networks and self-attention work using an intuitive, real-world metaphor.",
        icon = Icons.Default.Psychology,
        targetMode = AiMode.UNIFIED
    ),
    PromptItem(
        category = "Architecture",
        title = "System Architecture",
        prompt = "Design a scalable microservices architecture for a real-time messaging app handling 100k concurrent users.",
        icon = Icons.Default.AutoAwesome,
        targetMode = AiMode.UNIFIED
    )
)

@Composable
fun PromptSuggestions(
    onSelectPrompt: (prompt: String, mode: AiMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App branding & subtitle (Clean Minimalism)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = "Adish's chatboot",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Indigo50
            ) {
                Text(
                    text = "All-in-One",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Indigo700,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "A powerfull AI chatboot created byAdish Yadav",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Slate600,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Clean Minimalist starter prompt cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SamplePrompts.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectPrompt(item.prompt, item.targetMode) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Indigo50
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Indigo700,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.prompt,
                                fontSize = 12.sp,
                                color = Slate500,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

