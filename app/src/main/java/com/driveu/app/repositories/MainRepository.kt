package com.driveu.app.repositories

import com.driveu.app.db.Run
import com.driveu.app.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(private val dao: RunDao) {

    suspend fun insertRun(run: Run) = dao.insertRun(run)

    fun getAllRunsSortedByDate() = dao.getAllRunsSortedByDate()
}
