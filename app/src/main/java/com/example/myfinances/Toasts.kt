package com.example.myfinances

import android.content.Context
import android.widget.Toast

object Toasts {
    public val requestFailed: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Ошибка запроса", Toast.LENGTH_SHORT)
    }

    public val loginEmpty: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Введите логин", Toast.LENGTH_SHORT)
    }

    public val loginShort: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Логин слишком короткий", Toast.LENGTH_SHORT)
    }

    public val passwordEmpty: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Введите пароль", Toast.LENGTH_SHORT)
    }

    public val passwordShort: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Пароль слишком короткий", Toast.LENGTH_SHORT)
    }

    public val passwordConfirmEmpty: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Введите пароль повторно", Toast.LENGTH_SHORT)
    }

    public val passwordConfirmNotEqueal: (c: Context) ->
        Toast = { c -> Toast.makeText(c, "Пароли не совпадают", Toast.LENGTH_SHORT)
    }
}