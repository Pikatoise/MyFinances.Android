package com.example.myfinances

import android.content.Context
import android.widget.Toast

object Toasts {
    public val requestFailed: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Ошибка запроса", Toast.LENGTH_SHORT)
    }

    public val loginEmpty: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Введите логин", Toast.LENGTH_SHORT)
    }

    public val loginShort: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Логин слишком короткий", Toast.LENGTH_SHORT)
    }

    public val loginLong: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Логин слишком длинный", Toast.LENGTH_SHORT)
    }

    public val loginCantContainSpace: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Логин не может содержать пробелы", Toast.LENGTH_SHORT)
    }

    public val passwordEmpty: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Введите пароль", Toast.LENGTH_SHORT)
    }

    public val passwordShort: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Пароль слишком короткий", Toast.LENGTH_SHORT)
    }

    public val passwordLong: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Пароль слишком длинный", Toast.LENGTH_SHORT)
    }

    public val passwordCantContainSpace: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Пароль не может содержать пробелы", Toast.LENGTH_SHORT)
    }

    public val passwordConfirmEmpty: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Введите пароль повторно", Toast.LENGTH_SHORT)
    }

    public val passwordConfirmNotEqueal: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Пароли не совпадают", Toast.LENGTH_SHORT)
    }

    public val successfully: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Готово", Toast.LENGTH_SHORT)
    }

    public val failure: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Неудачно", Toast.LENGTH_SHORT)
    }

    public val titleEmpty: (c: Context) -> Toast = {
        c -> Toast.makeText(c, "Введите описание", Toast.LENGTH_SHORT)
    }

    public val titleLong: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Описание слишком длинное", Toast.LENGTH_SHORT)
    }

    public val amountEmpty: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Введите сумму", Toast.LENGTH_SHORT)
    }

    public val amountBig: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Сумма слишком большая\n   (лимит +-999 тыс.)", Toast.LENGTH_SHORT)
    }

    public val categoryEmpty: (c: Context) -> Toast = {
            c -> Toast.makeText(c, "Выберите категорию", Toast.LENGTH_SHORT)
    }
}