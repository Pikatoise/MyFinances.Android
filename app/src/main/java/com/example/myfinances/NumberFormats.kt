package com.example.myfinances

import java.text.DecimalFormat
import java.util.Locale

class NumberFormats {
	companion object {
		fun FormatToRuble(number: Double): String{
			val formatter = DecimalFormat.getNumberInstance(Locale("id","ID"))
			var result = formatter.format(number)

			result = result.replace('.',' ')
			result += " ₽"

			return result
		}
	}

}