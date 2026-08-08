package com.asistented.app.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal object DimensionesDiseno {
    val anchoContenido = 720.dp
    val margenPantalla = 16.dp
    val espacioPantalla = 12.dp
    val paddingTarjeta = 12.dp
    val altoAccion = 48.dp
    val objetivoTactil = 48.dp
    val iconoAccion = 24.dp
    val avatarEncabezado = 36.dp
}

@Composable
internal fun EncabezadoPantalla(
    titulo: String,
    descripcionRegresar: String,
    avatarId: String,
    onRegresar: () -> Unit,
    onAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
    colorIcono: Color = MaterialTheme.colorScheme.onBackground,
    mostrarDivisor: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRegresar,
                modifier = Modifier.size(DimensionesDiseno.objetivoTactil)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = descripcionRegresar,
                    modifier = Modifier.size(DimensionesDiseno.iconoAccion),
                    tint = colorIcono
                )
            }
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onAbrirPerfil,
                modifier = Modifier
                    .size(DimensionesDiseno.objetivoTactil)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                AvatarUsuario(
                    avatarId = avatarId,
                    modifier = Modifier.size(DimensionesDiseno.avatarEncabezado)
                )
            }
        }
        if (mostrarDivisor) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
internal fun AccionesTarjetaTramite(
    esFavorito: Boolean,
    textoAccion: String,
    descripcionFavorito: String,
    onAbrir: () -> Unit,
    onAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onAbrir,
            modifier = Modifier
                .weight(1f)
                .height(DimensionesDiseno.altoAccion),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = textoAccion,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        OutlinedIconButton(
            onClick = onAlternarFavorito,
            modifier = Modifier.size(DimensionesDiseno.altoAccion),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = if (esFavorito) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = descripcionFavorito,
                modifier = Modifier.size(DimensionesDiseno.iconoAccion)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AvisoModalDiseno(
    mensaje: String,
    textoBoton: String,
    descripcionIcono: String,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier,
    iconoConfirmacion: Boolean = false
) {
    val estadoHoja = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (iconoConfirmacion) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = descripcionIcono,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                    contentDescription = descripcionIcono,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = mensaje,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onContinuar,
                modifier = Modifier
                    .widthIn(max = 270.dp)
                    .fillMaxWidth()
                    .height(DimensionesDiseno.altoAccion),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(textoBoton, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun coloresCampoDiseno(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    errorLabelColor = MaterialTheme.colorScheme.error,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    errorLeadingIconColor = MaterialTheme.colorScheme.error
)
