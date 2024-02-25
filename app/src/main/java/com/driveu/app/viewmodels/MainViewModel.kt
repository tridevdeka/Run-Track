package com.driveu.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driveu.app.db.Run
import com.driveu.app.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repo: MainRepository) : ViewModel() {


    fun getAllRuns():LiveData<List<Run>>{
        return repo.getAllRunsSortedByDate()
    }


    fun insertRun(run: Run) = viewModelScope.launch {
        repo.insertRun(run)
    }


}