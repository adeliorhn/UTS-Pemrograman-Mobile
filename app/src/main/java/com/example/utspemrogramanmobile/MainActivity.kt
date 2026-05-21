package com.example.utspemrogramanmobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

// Data Models
data class MenuItem(
    val id: Int,
    val name: String,
    val price: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// SharedPreferences Constants
const val PREFS_NAME = "RestaurantPrefs"
const val KEY_NAME = "res_name"
const val KEY_ADDRESS = "res_address"
const val KEY_DESC = "res_desc"
const val KEY_HOURS = "res_hours"
const val KEY_DARK_MODE = "dark_mode"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            var isDarkMode by remember { 
                mutableStateOf(prefs.getBoolean(KEY_DARK_MODE, false)) 
            }

            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RestaurantApp(isDarkMode) {
                        isDarkMode = it
                        prefs.edit().putBoolean(KEY_DARK_MODE, it).apply()
                    }
                }
            }
        }
    }
}

// Navigation Routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Menu : Screen("menu")
    object DetailMenu : Screen("detail_menu/{menuId}") {
        fun createRoute(menuId: Int) = "detail_menu/$menuId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
}

@Composable
fun RestaurantApp(isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Splash.route) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(
                Screen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            ) { 
                HomeScreen(navController, isDarkMode, onThemeChange) 
            }
            composable(
                Screen.Menu.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) }
            ) { 
                MenuScreen(navController) 
            }
            composable(
                Screen.DetailMenu.route,
                arguments = listOf(navArgument("menuId") { type = NavType.IntType }),
                enterTransition = { scaleIn(initialScale = 0.8f) + fadeIn() },
                exitTransition = { scaleOut(targetScale = 0.8f) + fadeOut() }
            ) { backStackEntry ->
                val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0
                DetailMenuScreen(navController, menuId)
            }
            composable(Screen.Profile.route) { ProfileScreen(navController) }
            composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Beranda") },
            selected = currentDestination?.route == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
            label = { Text("Menu") },
            selected = currentDestination?.route == Screen.Menu.route,
            onClick = { navController.navigate(Screen.Menu.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profil") },
            selected = currentDestination?.route == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) }
        )
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selera Nusantara", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pengalaman Makan Modern", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController, isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val resName = prefs.getString(KEY_NAME, "Selera Nusantara") ?: "Selera Nusantara"
    
    val carouselItems = listOf(
        Icons.Default.LocalPizza to "Italia Otentik",
        Icons.Default.BakeryDining to "Baru Dipanggang",
        Icons.Default.Icecream to "Pencuci Mulut Manis"
    )
    val pagerState = rememberPagerState(pageCount = { carouselItems.size })

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Selamat Pagi!", fontSize = 16.sp, color = Color.Gray)
            IconButton(onClick = { onThemeChange(!isDarkMode) }) {
                Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = null)
            }
        }
        Text(resName, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HorizontalPager(state = pagerState, modifier = Modifier.height(220.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))) { page ->
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(carouselItems[page].first, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(carouselItems[page].second, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Menu.route) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Lihat Menu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Jelajahi hidangan lezat kami")
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Profile.route) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Profil Restoran", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Pelajari lebih lanjut tentang kami")
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun MenuScreen(navController: NavHostController) {
    val menuList = getDummyMenu()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Text("Menu Kami", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(menuList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.DetailMenu.createRoute(item.id)) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(item.price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMenuScreen(navController: NavHostController, menuId: Int) {
    val item = getDummyMenu().find { it.id == menuId } ?: return
    var rating by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(150.dp), tint = MaterialTheme.colorScheme.primary)
        }
        
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(item.price, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Deskripsi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(item.description, color = Color.Gray, lineHeight = 24.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Beri nilai hidangan ini", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Row {
                for (i in 1..5) {
                    IconButton(onClick = { rating = i }) {
                        Icon(
                            if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { navController.popBackStack() }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Kembali ke Menu", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    val name = prefs.getString(KEY_NAME, "Selera Nusantara") ?: "Selera Nusantara"
    val address = prefs.getString(KEY_ADDRESS, "Jl. Kuliner No. 123, Jakarta") ?: "Jl. Kuliner No. 123, Jakarta"
    val desc = prefs.getString(KEY_DESC, "Nikmati masakan modern terbaik dengan sentuhan cita rasa lokal.") ?: "Nikmati masakan modern terbaik dengan sentuhan cita rasa lokal."
    val hours = prefs.getString(KEY_HOURS, "10:00 - 22:00") ?: "10:00 - 22:00"

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.size(140.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(70.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(70.dp), tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(address, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(Icons.Default.Notes, "Deskripsi", desc)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(Icons.Default.Schedule, "Jam Operasional", hours)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navController.navigate(Screen.EditProfile.route) }, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profil")
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EditProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    var name by remember { mutableStateOf(prefs.getString(KEY_NAME, "Selera Nusantara") ?: "") }
    var address by remember { mutableStateOf(prefs.getString(KEY_ADDRESS, "Jl. Kuliner No. 123, Jakarta") ?: "") }
    var desc by remember { mutableStateOf(prefs.getString(KEY_DESC, "Nikmati masakan modern terbaik dengan sentuhan cita rasa lokal.") ?: "") }
    var hours by remember { mutableStateOf(prefs.getString(KEY_HOURS, "10:00 - 22:00") ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Edit Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Restoran") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Jam Operasional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        
        Spacer(modifier = Modifier.height(40.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Batal")
            }
            Button(onClick = {
                prefs.edit().apply {
                    putString(KEY_NAME, name)
                    putString(KEY_ADDRESS, address)
                    putString(KEY_DESC, desc)
                    putString(KEY_HOURS, hours)
                    apply()
                }
                navController.popBackStack()
            }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Simpan")
            }
        }
    }
}

fun getDummyMenu() = listOf(
    MenuItem(1, "Classic Wagyu Burger", "Rp 125.000", "Daging wagyu premium dengan mayo truffle, bawang karamel, dan keju cheddar.", Icons.Default.LunchDining),
    MenuItem(2, "Truffle Mushroom Pizza", "Rp 145.000", "Adonan buatan tangan dengan jamur liar, minyak truffle, dan mozzarella segar.", Icons.Default.LocalPizza),
    MenuItem(3, "Grilled Salmon Salad", "Rp 95.000", "Salmon Atlantik dengan sayuran campur, alpukat, dan saus madu mustard.", Icons.Default.Restaurant),
    MenuItem(4, "Artisan Coffee", "Rp 45.000", "Biji kopi arabika yang baru dipanggang dan diseduh dengan sempurna.", Icons.Default.Coffee),
    MenuItem(5, "Matcha Lava Cake", "Rp 55.000", "Kue matcha hangat dengan bagian tengah meleleh dan gelato vanila.", Icons.Default.Icecream)
)
