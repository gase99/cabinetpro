package com.cabinetpro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabinetpro.app.ui.components.RingLightCard
import com.cabinetpro.app.ui.theme.Muted
import com.cabinetpro.app.ui.theme.Navy

@Composable
fun HelpDialog(onDismiss: () -> Unit, onLoadSample: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("راهنمای محاسبه قیمت کابینت", fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "با سه قدم ساده، هزینه واقعی پروژه را برآورد کنید. تمام محاسبات زنده و روی دستگاه شما انجام می‌شود.",
                    fontSize = 12.sp, color = Muted
                )
                HelpStep("۱", "قیمت پایه و ضرایب", "قیمت هر متر کابینت زمینی را وارد کنید. ضرایب پیش‌فرض: زمینی ۱، دیواری ۰٫۶، ایستاده ۲ و جزیره ۱ هستند.")
                HelpStep("۲", "طول‌ها و کسرها", "طول هر بخش را به متر وارد کنید. کسرهای یخچال، گاز و سایر موارد فقط از طول زمینی کم می‌شوند.")
                HelpStep("۳", "هزینه‌ها و فاکتور", "صفحه، یراق‌آلات، نصب و حمل را اضافه کنید. تخفیف از جمع هزینه‌ها کسر و سپس مالیات محاسبه می‌شود.")
                HelpStep("۴", "محاسبات ویژه", "کابینت دو پله و کافی بار را با فرمول‌های مخصوص محاسبه کنید. در ماژول کمد، مدل را از منوی مدل‌های موجود در بازار ایران انتخاب کنید؛ هر مدل ضریب و فرمول مخصوص به خود را دارد و در صورت فعال‌شدن، در فاکتور نهایی گنجانده می‌شود.")
                RingLightCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, borderWidth = 2.dp, phaseOffset = 60f) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("فرمول اصلی محاسبه", fontWeight = FontWeight.Bold, color = Navy, fontSize = 12.sp)
                        Text("متراژ مؤثر = طول بخش × ضریب بخش", fontSize = 11.sp, color = Navy)
                        Text("هزینه کابینت = مجموع متراژ مؤثر × قیمت پایه", fontSize = 11.sp, color = Navy)
                    }
                }
                RingLightCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, borderWidth = 2.dp, phaseOffset = 120f) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("نمونه تست پذیرش", fontWeight = FontWeight.Bold, color = Navy, fontSize = 12.sp)
                        Text("قیمت پایه ۶٬۵۰۰٬۰۰۰ تومان؛ زمینی ۳ متر، دیواری ۳ متر، ایستاده ۱ متر.", fontSize = 11.sp, color = Navy)
                        Text("متراژ مؤثر: ۳ + ۱٫۸ + ۲ = ۶٫۸ متر", fontSize = 11.sp, color = Navy)
                        Text("هزینه کابینت: ۴۴٬۲۰۰٬۰۰۰ تومان", fontSize = 11.sp, color = Navy)
                    }
                }
                RingLightCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, borderWidth = 2.dp, phaseOffset = 180f) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("فرمول محاسبات ویژه", fontWeight = FontWeight.Bold, color = Navy, fontSize = 12.sp)
                        Text("کابینت دو پله: طول پله ۱ × قیمت پله ۱ + طول پله ۲ × قیمت پله ۲", fontSize = 11.sp, color = Navy)
                        Text("کافی بار: قیمت پایه + طول × نرخ + طول صفحه × نرخ صفحه", fontSize = 11.sp, color = Navy)
                        Text("کمد: عرض × قیمت هر متر × ضریب مدل + اجناس (کشوی، قفسه)", fontSize = 11.sp, color = Navy)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onLoadSample) { Text("بارگذاری نمونه محاسبه") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("متوجه شدم") } }
    )
}

@Composable
private fun HelpStep(num: String, title: String, desc: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$num — $title", fontWeight = FontWeight.Bold, color = Navy, fontSize = 12.sp)
        Text(desc, fontSize = 11.sp, color = Muted)
    }
}
