package zt.tau.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zt.tau.ui.component.sidepanel.Bookmark
import zt.tau.ui.window.currentLocation
import java.io.File

@Composable
fun SidePanel() {
    Surface(
        modifier = Modifier.fillMaxHeight(),
        tonalElevation = 5.dp
    ) {
        val roots = remember { File.listRoots() }

        LazyColumn(
            modifier = Modifier.width(160.dp), // eventually fetch from settings
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = roots,
                key = { it.hashCode() }
            ) {
                Bookmark(
                    zt.tau.model.Bookmark(it.toPath(), it.absolutePath),
                    icon = Icons.Default.Storage,
                    onClick = {
                        currentLocation = it.toPath()
                    }
                )
            }
        }
    }
}