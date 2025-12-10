package org.th.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.City;
import org.th.entity.District;
import org.th.repository.CityRepository;
import org.th.repository.DistrictRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;

    @PostConstruct
    @Transactional
    public void init() {
        seedBangkokDistricts();
    }

    private void seedBangkokDistricts() {
        City bangkok = cityRepository.findBySlug("bangkok").orElse(null);
        if (bangkok != null && districtRepository.findByCityIdAndActiveTrue(bangkok.getId()).isEmpty()) {
            log.info("Seeding Bangkok districts...");
            List<District> districts = new ArrayList<>();
            String[][] data = {
                    { "Phra Nakhon", "พระนคร", "ဖရာနခွန်" },
                    { "Dusit", "ดุสิต", "ဒူဆစ်" },
                    { "Nong Chok", "หนองจอก", "နောင်ချော့" },
                    { "Bang Rak", "บางรัก", "ဘန်ရက်" },
                    { "Bang Khen", "บางเขน", "ဘန်ခိန်း" },
                    { "Bang Kapi", "บางกะปิ", "ဘန်ကပိ" },
                    { "Pathum Wan", "ปทุมวัน", "ပသွမ်ဝမ်" },
                    { "Pom Prap Sattru Phai", "ป้อมปราบศัตรูพ่าย", "ပွန်ပရပ်ဆတ်ထရူဖိုင်" },
                    { "Phra Khanong", "พระโขนง", "ဖရာခနုံ" },
                    { "Min Buri", "มีนบุรี", "မင်းဘူရီ" },
                    { "Lat Krabang", "ลาดกระบัง", "လတ်ក្រဘန်" },
                    { "Yan Nawa", "ยานนาวา", "ရန်နဝါ" },
                    { "Samphanthawong", "สัมพันธวงศ์", "ဆမ်ဖန်ထဝုန်" },
                    { "Phaya Thai", "พญาไท", "ဖယာထိုင်း" },
                    { "Thon Buri", "ธนบุรี", "သွန်ဘူရီ" },
                    { "Bangkok Yai", "บางกอกใหญ่", "ဘန်ကောက်ယိုင်" },
                    { "Huai Khwang", "ห้วยขวาง", "ဟွေခဝမ်" },
                    { "Khlong San", "คลองสาน", "ခလုံဆန်" },
                    { "Taling Chan", "ตลิ่งชัน", "တလိမ်ချန်" },
                    { "Bangkok Noi", "บางกอกน้อย", "ဘန်ကောက်နွိုင်း" },
                    { "Bang Khun Thian", "บางขุนเทียน", "ဘန်ခုန်ထီယန်" },
                    { "Phasi Charoen", "ภาษีเจริญ", "ဖာဆီချာရွန်း" },
                    { "Nong Khaem", "หนองแขม", "နောင်ခမ်" },
                    { "Rat Burana", "ราษฎร์บูรณะ", "ရတ်ဘူရန" },
                    { "Bang Phlat", "บางพลัด", "ဘန်ဖလပ်" },
                    { "Din Daeng", "ดินแดง", "ဒင်ဒန်း" },
                    { "Bueng Kum", "บึงกุ่ม", "ဘွန်းကုမ်" },
                    { "Sathon", "สาทร", "စသွန်" },
                    { "Bang Sue", "บางซื่อ", "ဘန်ဆူ" },
                    { "Chatuchak", "จตุจักร", "ကျတူဂျက်" },
                    { "Bang Kho Laem", "บางคอแหลม", "ဘန်ခိုလမ်" },
                    { "Prawet", "ประเวศ", "ပရာဝက်" },
                    { "Khlong Toei", "คลองเตย", "ခလုံတေ့" },
                    { "Suan Luang", "สวนหลวง", "ဆွန်လောင်" },
                    { "Chom Thong", "จอมทอง", "ချွန်ထောင်" },
                    { "Don Mueang", "ดอนเมือง", "ဒွန်မောင်း" },
                    { "Ratchathewi", "ราชเทวี", "ရာ့ချ်ထေဝီ" },
                    { "Lat Phrao", "ลาดพร้าว", "လတ်ဖလောင်း" },
                    { "Watthana", "วัฒนา", "ဝတ်ထနာ" },
                    { "Bang Khae", "บางแค", "ဘန်ခဲ" },
                    { "Lak Si", "หลักสี่", "လက်စီ" },
                    { "Sai Mai", "สายไหม", "ဆိုင်းမိုင်" },
                    { "Khan Na Yao", "คันนายาว", "ခန်နာယောင်" },
                    { "Saphan Sung", "สะพานสูง", "ဆဖန်ဆုန်" },
                    { "Wang Thonglang", "วังทองหลาง", "ဝမ်ထောင်လမ်" },
                    { "Khlong Sam Wa", "คลองสามวา", "ခလုံဆမ်ဝါ" },
                    { "Bang Na", "บางนา", "ဘန်နာ" },
                    { "Thawi Watthana", "ทวีวัฒนา", "ထဝီဝတ်ထနာ" },
                    { "Thung Khru", "ทุ่งครุ", "ထုန်ခရု" },
                    { "Bang Bon", "บางบอน", "ဘန်ဘွန်" }
            };

            for (String[] d : data) {
                districts.add(District.builder()
                        .city(bangkok)
                        .nameEn(d[0])
                        .nameTh(d[1])
                        .nameMm(d[2])
                        .slug(d[0].toLowerCase().replace(" ", "-"))
                        .active(true)
                        .build());
            }
            districtRepository.saveAll(districts);
            log.info("Seeded {} districts for Bangkok.", districts.size());
        }
    }

    public List<District> getDistrictsByCity(Long cityId) {
        return districtRepository.findByCityIdAndActiveTrue(cityId);
    }
}
