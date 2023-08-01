/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.rally

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.rally.ui.accounts.AccountsScreen
import com.example.compose.rally.ui.bills.BillsScreen
import com.example.compose.rally.ui.components.RallyTabRow
import com.example.compose.rally.ui.overview.OverviewScreen
import com.example.compose.rally.ui.theme.RallyTheme

/**
 * This Activity recreates part of the Rally Material Study from
 * https://material.io/design/material-studies/rally.html
 */
class RallyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RallyApp()
        }
    }
}

// NavControllerは常に階層最上位のRoot Composableに配置する。
// これによりNavControllerを参照する必要のあるすべての下階層のComposableがアクセスできるようになる
@Composable
fun RallyApp() {
    RallyTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        Log.d("RallyApp", "currentBackStack=$currentBackStack")
        val currentDestination = currentBackStack?.destination
        Log.d("RallyApp", "currentDestination=$currentDestination")
        val currentScreen = rallyTabRowScreens.find { it.route == currentDestination?.route } ?: Overview
        Scaffold(
            topBar = {
                RallyTabRow(
                    allScreens = rallyTabRowScreens,
                    // コードを再利用が可能なものにするため、navController全体をコンポーザブルに渡すことはせず、
                    // トリガーするナビゲーションアクションを定義するコールバックを常に指定する
                    onTabSelected = { newScreen ->
                        navController.navigateSingleTopTo(newScreen.route)
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            // NavHost はコンテナとして機能し、ナビゲーショングラフの現在のデスティネーションを表示する
            // NavController は常に 1 つの NavHost コンポーザブルに関連付けられる
            // コンポーザブル間を移動すると、NavHostのコンテンツは自動的に再コンポーズされる
            NavHost(
                navController = navController,
                startDestination = Overview.route,
                modifier = Modifier.padding(innerPadding),
                // ナビゲーション グラフを定義してビルドするためのもの
                builder = {
                    composable(
                        route = Overview.route,
                        content = {
                            OverviewScreen(
                                onClickSeeAllAccounts = { navController.navigateSingleTopTo(Accounts.route) },
                                onClickSeeAllBills = { navController.navigateSingleTopTo(Bills.route) }
                            )
                        }
                    )
                    composable(
                        route = Accounts.route,
                        content = {
                            AccountsScreen()
                        }
                    )
                    composable(
                        route = Bills.route,
                        content = {
                            BillsScreen()
                        }
                    )
                }
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    // Todo: この辺のバックスタックについて確認必要、また、thisの意味がよくわからん
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }