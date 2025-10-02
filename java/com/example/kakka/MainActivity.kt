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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kakka.ui.theme.KAKKATheme
// Asumsi Calc sudah dipindahkan ke package com.example.kakka.

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
        color = MaterialTheme.colorScheme.background
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
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = displayValue,
                    fontSize = 32.sp,
                    textAlign = TextAlign.End
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
    val operatorColor = Color(0xFFFF9800)
    val specialColor = Color(0xFFFF9800)
    val digitColor = Color(0xFFF0F0F0)

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

    // Helper function to handle digit/constant logic
    fun getDigitAction(buttonText: String): () -> Unit {
        return {
            calc.onDigit(buttonText)
            onDisplayUpdate()
        }
    }

    // Helper function to handle scientific function logic
    fun getScientificAction(buttonText: String): () -> Unit {
        return {
            calc.onScientificFunction(buttonText)
            onDisplayUpdate()
        }
    }

    // --- Scientific Keypad (Jika isSciMode TRUE) ---
    if (isSciMode) {
        // Baris Ilmiah 1: log, ln, sin, cos, tan
        Row(modifier = Modifier.fillMaxWidth()) {
            OperatorButton("log", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("log"))
            OperatorButton("ln", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("ln"))
            OperatorButton("sin", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("sin"))
            OperatorButton("cos", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("cos"))
            OperatorButton("tan", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("tan"))
            OperatorButton("%", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("%"))

        }

        // Baris Ilmiah 2: sqrt, asin, acos, atan, x!
        Row(modifier = Modifier.fillMaxWidth()) {
            OperatorButton("sqrt", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("sqrt"))
            OperatorButton("asin", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("asin"))
            OperatorButton("acos", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("acos"))
            OperatorButton("atan", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("atan"))
            OperatorButton("x!", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("x!"))
            OperatorButton("1/x", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = getScientificAction("1/x"))

        }
    }

    // --- Main Keypad ---

    // BARIS 1: AC, Del, x^y, / (dan tombol ilmiah 1/x jika SciMode)
    Row(modifier = Modifier.fillMaxWidth()) {


        // AC
        OperatorButton("AC", modifier = Modifier.weight(1f), buttonColor = specialColor) {
            calc.onClear()
            onDisplayUpdate()
        }

        // Del (Menggunakan Ikon)
        OperatorButton(
            textButton = "Del",
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.Backspace,
            buttonColor = specialColor
        ) {
            calc.onDelete()
            onDisplayUpdate()
        }

        // x^y
        OperatorButton("^", modifier = Modifier.weight(1f), buttonColor = operatorColor, onClick = getOperatorAction("^"))

        // /
        OperatorButton("/", modifier = Modifier.weight(1f), buttonColor = operatorColor, onClick = getOperatorAction("/"))
    }

    // BARIS 2: 7, 8, 9, ×
    Row(modifier = Modifier.fillMaxWidth()) {
        NumButton("7", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("7"))
        NumButton("8", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("8"))
        NumButton("9", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("9"))
        OperatorButton("x", modifier = Modifier.weight(1f), buttonColor = operatorColor, onClick = getOperatorAction("x"))
    }

    // BARIS 3: 4, 5, 6, −
    Row(modifier = Modifier.fillMaxWidth()) {
        NumButton("4", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("4"))
        NumButton("5", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("5"))
        NumButton("6", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("6"))
        OperatorButton("−", modifier = Modifier.weight(1f), buttonColor = operatorColor, onClick = getOperatorAction("−"))
    }

    // BARIS 4: 1, 2, 3, + (Tombol 'e' dihapus)
    Row(modifier = Modifier.fillMaxWidth()) {
        // Tombol 'e' (scientific) dihapus
        NumButton("1", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("1"))
        NumButton("2", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("2"))
        NumButton("3", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("3"))
        OperatorButton("+", modifier = Modifier.weight(1f), buttonColor = operatorColor, onClick = getOperatorAction("+"))
    }

    // BARIS 5: Sc, 0, ., = (Tombol '%' dihapus)
    Row(modifier = Modifier.fillMaxWidth()) {
        // Tombol '%' (scientific) dihapus

        // Sc (Toggle Scientific Mode)
        OperatorButton("Sc", modifier = Modifier.weight(1f), buttonColor = specialColor, onClick = onToggleSciMode)

        NumButton("0", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("0"))
        NumButton(".", modifier = Modifier.weight(1f), digitColor = digitColor, onClick = getDigitAction("."))

        // =
        OperatorButton("=", modifier = Modifier.weight(1f), buttonColor = operatorColor) {
            calc.onEquals()
            onDisplayUpdate()
        }
    }
}

// Tombol Basis: Menerima Modifier sebagai argumen, tidak lagi menggunakan RowScope
@Composable
fun BaseCalcButton(
    modifier: Modifier = Modifier, // Menerima Modifier dari luar
    onClick: () -> Unit,
    background: Color,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        // Menerapkan Modifier yang diberikan dari pemanggil
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background),
        contentPadding = PaddingValues(0.dp),
        content = content
    )
}

// 1. Tombol Digit (sebelumnya DigitButton)
@Composable
fun NumButton(
    textButton: String,
    modifier: Modifier = Modifier, // Menerima Modifier
    digitColor: Color,
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
            color = Color.Black
        )
    }
}

// 2. Tombol Operasi (sebelumnya OperationButton)
@Composable
fun OperatorButton(
    textButton: String,
    modifier: Modifier = Modifier, // Menerima Modifier
    icon: ImageVector? = null,
    buttonColor: Color,
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
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = textButton,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
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