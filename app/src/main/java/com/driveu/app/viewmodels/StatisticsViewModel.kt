package com.driveu.app.viewmodels

import androidx.lifecycle.ViewModel
import com.driveu.app.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(val repo: MainRepository) : ViewModel() {
}