package ru.kraz.messageread

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(module)
        }
    }
}

val module = module {
    single<FirebaseDatabase> {
        FirebaseDatabase.getInstance()
    }

    viewModel<MainViewModel> {
        MainViewModel(get())
    }
}