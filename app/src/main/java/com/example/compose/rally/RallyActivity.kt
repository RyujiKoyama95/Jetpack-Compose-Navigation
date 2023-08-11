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
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.compose.rally.ui.accounts.AccountsScreen
import com.example.compose.rally.ui.accounts.SingleAccountScreen
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
                                onClickSeeAllBills = { navController.navigateSingleTopTo(Bills.route) },
                                onAccountClick = { accountType ->
                                    navController.navigateSingleAccount(accountType)
                                }
                            )
                        }
                    )
                    composable(
                        route = Accounts.route,
                        content = {
                            AccountsScreen(
                                onAccountClick = { accountType ->
                                    navController.navigateSingleAccount(accountType)
                                }
                            )
                        }
                    )
                    composable(
                        route = Bills.route,
                        content = {
                            BillsScreen()
                        }
                    )
                    composable(
                        route = SingleAccount.routeWithArgs,
                        arguments = SingleAccount.arguments,
                        deepLinks = SingleAccount.deepLinks,
                        content = { navBackStackEntry ->
                            val accountType =
                                navBackStackEntry.arguments?.getString(SingleAccount.accountTypeArg)
                            Log.d("RallyApp", "accountType=$accountType")
                            SingleAccountScreen(
                                accountType = accountType
                            )
                        }
                    )
                }
            )
        }
    }
}

/**
 * this@navigateSingleTopTo.graph.findStartDestination().id は、ナビゲーショングラフ内のスタート（最初の）目的地（画面）のIDを取得しています。
 * navigateSingleTopToメソッドが実行されるときには、このIDが指定されたルート（目的地）に対応します。
 * popUpToメソッドの第一引数には、バックスタックから削除したい画面のIDを指定します。上記のコードでは、
 * 最初の目的地までバックスタックをクリア（削除）するために、スタート目的地のIDが指定されています。
 * popUpToメソッドのブロック内では、saveStateプロパティがtrueに設定されています。
 * これは、削除する画面の状態を保存するかどうかを指定するもので、trueに設定されている場合、その画面の状態が保存されます。
 * これにより、画面が再作成されるときに以前の状態を復元することができます。
 * 要するに、このコードは「特定の目的地までバックスタックをクリアして移動する」というナビゲーションの振る舞いを制御しています。
 */
private fun NavHostController.navigateSingleTopTo(route: String) =
    // thisはインスタンスを指すのでこの場合はNavHostController
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

private fun NavHostController.navigateSingleAccount(accountType: String) {
    this.navigateSingleTopTo("${SingleAccount.route}/$accountType")
}