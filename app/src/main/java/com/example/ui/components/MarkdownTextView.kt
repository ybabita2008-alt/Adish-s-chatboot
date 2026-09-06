package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Code(val language: String, val code: String) : ContentBlock()
}

@Composable
fun MarkdownTextView(
    content: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is ContentBlock.Code -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                is ContentBlock.Text -> {
                    RenderTextBlock(text = block.content, isUser = isUser)
                }
            }
        }
    }
}

@Composable
private fun RenderTextBlock(text: String, isUser: Boolean) {
    val lines = text.split("\n")
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = trimmed.removePrefix("### "),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                trimmed.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trimmed.removePrefix("## "),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = trimmed.removePrefix("# "),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 8.dp, start = 4.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = parseInlineFormatting(
                                text = trimmed.substring(2),
                                isUser = isUser
                            ),
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIndex = trimmed.indexOf('.')
                    val number = trimmed.substring(0, dotIndex + 1)
                    val rest = trimmed.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = number,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUser) textColor else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineFormatting(text = rest, isUser = isUser),
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text = parseInlineFormatting(text = line, isUser = isUser),
                        fontSize = 14.sp,
                        color = textColor,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

private fun parseInlineFormatting(text: String, isUser: Boolean) = buildAnnotatedString {
    val pattern = Regex("(\\*\\*.*?\\*\\*|`.*?`|\\*.*?\\*)")
    var lastIndex = 0

    val codeBg = if (isUser) Color(0x33FFFFFF) else Color(0xFFF1F5F9)
    val codeColor = if (isUser) Color.White else Color(0xFF0F172A)

    pattern.findAll(text).forEach { matchResult ->
        val start = matchResult.range.first
        val end = matchResult.range.last + 1

        if (start > lastIndex) {
            append(text.substring(lastIndex, start))
        }

        val match = matchResult.value
        when {
            match.startsWith("**") && match.endsWith("**") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.removeSurrounding("**"))
                }
            }
            match.startsWith("`") && match.endsWith("`") -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBg,
                        color = codeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(" ${match.removeSurrounding("`")} ")
                }
            }
            match.startsWith("*") && match.endsWith("*") -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.removeSurrounding("*"))
                }
            }
            else -> {
                append(match)
            }
        }
        lastIndex = end
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

private fun parseMarkdownBlocks(input: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val codeRegex = Regex("```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)```")

    var lastIndex = 0
    codeRegex.findAll(input).forEach { match ->
        val startIndex = match.range.first
        val endIndex = match.range.last + 1

        if (startIndex > lastIndex) {
            val text = input.substring(lastIndex, startIndex).trim()
            if (text.isNotEmpty()) {
                blocks.add(ContentBlock.Text(text))
            }
        }

        val lang = match.groupValues[1].trim()
        val code = match.groupValues[2].trimEnd()
        blocks.add(ContentBlock.Code(language = lang, code = code))

        lastIndex = endIndex
    }

    if (lastIndex < input.length) {
        val remaining = input.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            blocks.add(ContentBlock.Text(remaining))
        }
    }

    if (blocks.isEmpty() && input.isNotBlank()) {
        blocks.add(ContentBlock.Text(input))
    }

    return blocks
}
