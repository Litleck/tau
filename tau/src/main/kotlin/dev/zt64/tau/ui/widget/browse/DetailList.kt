package dev.zt64.tau.ui.widget.browse

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import dev.zt64.tau.model.DetailColumnType
import dev.zt64.tau.model.Direction
import dev.zt64.tau.resources.*
import dev.zt64.tau.ui.component.menu.ItemContextMenu
import dev.zt64.tau.ui.viewmodel.BrowserViewModel
import dev.zt64.tau.util.dirSize
import dev.zt64.tau.util.humanReadableSize
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

@Composable
fun DetailList(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<BrowserViewModel>()

    val columns = remember {
        listOf(
            DetailColumnType.NAME,
            DetailColumnType.TYPE,
            DetailColumnType.SIZE
        )
    }
    val scope = rememberCoroutineScope()

    Table(
        modifier = modifier.fillMaxWidth(),
        columnCount = columns.size,
        rowCount = viewModel.contents.size,
        row = { rowIndex, content ->
            val path = viewModel.contents[rowIndex]

            ItemContextMenu(path) {
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val selected by remember(viewModel.selected) {
                    derivedStateOf { path in viewModel.selected }
                }

                Surface(
                    modifier = Modifier
                        .hoverable(interactionSource)
                        .selectable(
                            selected = selected,
                            onClick = {
                                viewModel.selectItems(path)
                            },
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        )
                        .combinedClickable(
                            onClick = {},
                            onDoubleClick = {
                                scope.launch {
                                    viewModel.open(path)
                                }
                            }
                        ),
                    color = if (rowIndex % 2 == 0) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.surfaceColorAtElevation(0.5.dp)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        content()
                    }
                }
            }
        },
        headerRow = { content ->
            Surface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    content()
                }
            }
        },
        cellContent = { columnIndex, rowIndex ->
            val item = viewModel.contents[rowIndex]

            val column = columns[columnIndex]

            when (column) {
                DetailColumnType.NAME -> {
                    Row {
                        Icon(
                            imageVector = if (item.isDirectory()) {
                                Icons.Default.Folder
                            } else {
                                Icons.Default.FilePresent
                            },
                            contentDescription = null
                        )

                        Text(
                            text = item.name
                        )
                    }
                }
                DetailColumnType.TYPE -> {
                    Text(
                        text = if (item.isDirectory()) {
                            "Directory"
                        } else {
                            try {
                                Files.probeContentType(item)!!
                            } catch (_: Exception) {
                                "Unknown"
                            }
                        }
                    )
                }
                DetailColumnType.SIZE -> {
                    Text(
                        text = if (item.isDirectory()) {
                            val itemCount = remember {
                                item.dirSize()
                            }

                            pluralStringResource(Res.plurals.items, itemCount ?: 0, itemCount ?: "?")
                        } else {
                            item.humanReadableSize()
                        }
                    )
                }
                else -> {}
            }
        },
        headerContent = { columnIndex ->
            val column = columns[columnIndex]

            Row(
                modifier = Modifier.clickable {
                    viewModel.sortBy(column)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (column) {
                    DetailColumnType.NAME -> {
                        Text( stringResource(Res.string.name) )
                    }
                    DetailColumnType.TYPE -> {
                        Text(stringResource(Res.string.type))
                    }
                    DetailColumnType.SIZE -> {
                        Text(stringResource(Res.string.size))
                    }
                    else -> {}
                }

                if (viewModel.sortType == column) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        imageVector = if (viewModel.sortDirection == Direction.ASCENDING) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.ArrowDownward
                        },
                        contentDescription = null
                    )
                }
            }
        }
    )
    // LazyColumn(modifier = modifier) {
    //     stickyHeader {
    //         Surface {
    //             HeaderRow(nameWeight, typeWeight, sizeWeight)
    //         }
    //     }
    //
    //     itemsIndexed(
    //         items = viewModel.contents
    //     ) { index, path ->
    //         val interactionSource = remember { MutableInteractionSource() }
    //         val isHovered by interactionSource.collectIsHoveredAsState()
    //         val selected by remember(viewModel.selected) {
    //             derivedStateOf {
    //                 viewModel.selected.contains(path)
    //             }
    //         }
    //
    //         ItemRow(
    //             modifier = Modifier
    //                 .hoverable(interactionSource)
    //                 .selectable(
    //                     selected = selected,
    //                     onClick = {
    //                         viewModel.selectItems(path)
    //                     },
    //                     interactionSource = interactionSource,
    //                     indication = LocalIndication.current
    //                 )
    //                 .combinedClickable(
    //                     onClick = {},
    //                     onDoubleClick = {
    //                         scope.launch {
    //                             viewModel.open(path)
    //                         }
    //                     }
    //                 ),
    //             item = path,
    //             nameWeight = nameWeight,
    //             typeWeight = typeWeight,
    //             sizeWeight = sizeWeight,
    //             onClick = {
    //             },
    //             color = if (index % 2 == 0) {
    //                 MaterialTheme.colorScheme.background
    //             } else {
    //                 MaterialTheme.colorScheme.surfaceColorAtElevation(0.5.dp)
    //             },
    //             interactionSource = interactionSource
    //         )
    //     }
    // }
}

@Composable
fun HeaderRow(
    nameWeight: Float,
    typeWeight: Float,
    sizeWeight: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Name", Modifier.weight(nameWeight))
        Text("Type", Modifier.weight(typeWeight))
        Text("Size", Modifier.weight(sizeWeight))
    }
}

@Composable
fun ItemRow(
    item: Path,
    nameWeight: Float,
    typeWeight: Float,
    sizeWeight: Float,
    onClick: () -> Unit = {},
    color: Color,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = if (item.isDirectory()) {
                    Icons.Default.Folder
                } else {
                    Icons.Default.FilePresent
                },
                contentDescription = null
            )
            Text(item.name, Modifier.weight(nameWeight))
            Text(
                modifier = Modifier.weight(typeWeight),
                text = if (item.isDirectory()) {
                    "Directory"
                } else {
                    try {
                        Files.probeContentType(item)!!
                    } catch (_: Exception) {
                        "Unknown"
                    }
                }
            )

            Text(
                modifier = Modifier.weight(sizeWeight),
                text = if (item.isDirectory()) {
                    val itemCount = remember {
                        item.dirSize()
                    }

                    pluralStringResource(Res.plurals.items, itemCount ?: 0, itemCount ?: "?")
                } else {
                    item.humanReadableSize()
                }
            )
        }
    }
}

@Composable
private fun Table(
    columnCount: Int,
    rowCount: Int,
    modifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    row: @Composable (rowIndex: Int, content: @Composable RowScope.() -> Unit) -> Unit,
    headerRow: @Composable (content: @Composable RowScope.() -> Unit) -> Unit,
    cellContent: @Composable RowScope.(columnIndex: Int, rowIndex: Int) -> Unit,
    headerContent: @Composable RowScope.(columnIndex: Int) -> Unit
) {
    Box(
        modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))
    ) {
        val columnWidths = remember { mutableStateMapOf<Int, Int>() }

        LazyColumn(
            modifier = Modifier.matchParentSize(),
            state = verticalLazyListState
        ) {
            stickyHeader {
                headerRow {
                    (0 until columnCount).forEach { columnIndex ->
                        Box(
                            modifier = Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)

                                val existingWidth = columnWidths[columnIndex] ?: 0
                                val maxWidth = maxOf(existingWidth, placeable.width)

                                if (maxWidth > existingWidth) {
                                    columnWidths[columnIndex] = maxWidth
                                }

                                layout(width = maxWidth, height = placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                        ) {
                            this@headerRow.headerContent(columnIndex)
                        }
                    }
                }
            }

            items(rowCount) { rowIndex ->
                row(rowIndex) {
                    (0 until columnCount).forEach { columnIndex ->
                        Box(
                            modifier = Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)

                                val existingWidth = columnWidths[columnIndex] ?: 0
                                val maxWidth = maxOf(existingWidth, placeable.width)

                                if (maxWidth > existingWidth) {
                                    columnWidths[columnIndex] = maxWidth
                                }

                                layout(width = maxWidth, height = placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                        ) {
                            with(this@row) {
                                if (columnIndex == 0) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        this@row.cellContent(columnIndex, rowIndex)
                                    }
                                } else {
                                    this@row.cellContent(columnIndex, rowIndex)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}