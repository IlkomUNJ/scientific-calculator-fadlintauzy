package com.example.kakka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kakka.ui.theme.KAKKATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KAKKATheme {
                SetupLayout()
            }
        }
    }
}

@Composable
fun SetupLayout() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Latar belakang hitam
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            var isSciMode by remember { mutableStateOf(false) }

            val calc = remember { Calc() }
            var displayValue by remember { mutableStateOf(calc.getDisplay()) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = displayValue,
                    fontSize = 46.sp,
                    textAlign = TextAlign.End,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }

            // Keypad Area
            CalculatorKeypad(
                calc = calc,
                isSciMode = isSciMode,
                onDisplayUpdate = { displayValue = calc.getDisplay() },
                onToggleSciMode = { isSciMode = !isSciMode }
            )
        }
    }
}

@Composable
fun CalculatorKeypad(
    calc: Calc,
    isSciMode: Boolean,
    onDisplayUpdate: () -> Unit,
    onToggleSciMode: () -> Unit
) {
    val specialColor = Color(0xFFFF9800)
    val digitColor = Color(0xFF333333)

    // Helper function to handle operator logic
    fun getOperatorAction(buttonText: String): () -> Unit {
        val operatorToSend = when (buttonText) {
            "x" -> "x"
            "−" -> "-"
            "^" -> "^"
            else -> buttonText
        }
        return {
            calc.onOperator(operatorToSend)
            onDisplayUpdate()
        }
    }

    fun getDigitAction(buttonText: String): () -> Unit {
        return {
            calc.onDigit(buttonText)
            onDisplayUpdate()
        }
    }

    fun getScientificAction(buttonText: String): () -> Unit {
        return {
            calc.onScientificFunction(buttonText)
            onDisplayUpdate()
        }
    }

    if (isSciMode) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OperatorButton("log", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("log"))
            OperatorButton("ln", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("ln"))
            OperatorButton("sin", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("sin"))
            OperatorButton("cos", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("cos"))
            OperatorButton("tan", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("tan"))
            OperatorButton("%", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("%"))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OperatorButton("sqrt", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("sqrt"))
            OperatorButton("asin", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("asin"))
            OperatorButton("acos", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("acos"))
            OperatorButton("atan", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("atan"))
            OperatorButton("x!", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("x!"))
            OperatorButton("1/x", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getScientificAction("1/x"))
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Tombol spesial
        OperatorButton("AC", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor) {
            calc.onClear()
            onDisplayUpdate()
        }

        OperatorButton(
            textButton = "Del",
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.Backspace,
            buttonColor = digitColor,
            iconTint = specialColor
        ) {
            calc.onDelete()
            onDisplayUpdate()
        }

        // Operator biner
        OperatorButton("^", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getOperatorAction("^"))
        OperatorButton("/", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getOperatorAction("/"))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        NumButton("7", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("7"))
        NumButton("8", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("8"))
        NumButton("9", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("9"))
        OperatorButton("x", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getOperatorAction("x"))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        NumButton("4", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("4"))
        NumButton("5", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("5"))
        NumButton("6", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("6"))
        OperatorButton("−", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getOperatorAction("−"))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        NumButton("1", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("1"))
        NumButton("2", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("2"))
        NumButton("3", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("3"))
        OperatorButton("+", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = getOperatorAction("+"))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Tombol Sci/Toggle
        OperatorButton("Sc", modifier = Modifier.weight(1f), buttonColor = digitColor, textColor = specialColor, onClick = onToggleSciMode)

        // Angka dan Desimal
        NumButton("0", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("0"))
        NumButton(".", modifier = Modifier.weight(1f), digitColor = digitColor, textColor = Color.White, onClick = getDigitAction("."))

        // Tombol Sama Dengan
        OperatorButton("=", modifier = Modifier.weight(1f), buttonColor = specialColor, textColor = Color.White) {
            calc.onEquals()
            onDisplayUpdate()
        }
    }
}

@Composable
fun BaseCalcButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    background: Color,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        // GUNAKAN parameter 'background' di sini
        colors = ButtonDefaults.buttonColors(containerColor = background),
        contentPadding = PaddingValues(0.dp),
        content = content
    )
}

@Composable
fun NumButton(
    textButton: String,
    modifier: Modifier = Modifier,
    digitColor: Color,
    textColor: Color,
    onClick: () -> Unit = {}
) {
    BaseCalcButton(
        modifier = modifier,
        onClick = onClick,
        background = digitColor
    ) {
        Text(
            text = textButton,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

@Composable
fun OperatorButton(
    textButton: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    buttonColor: Color,
    textColor: Color = Color.Black,
    iconTint: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    BaseCalcButton(
        modifier = modifier,
        onClick = onClick,
        background = buttonColor
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = textButton,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = textButton,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CalcPreview() {
    KAKKATheme {
        SetupLayout()
    }
}