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
                    { "Phra Nakhon", "พระนคร" }, { "Dusit", "ดุสิต" }, { "Nong Chok", "หนองจอก" },
                    { "Bang Rak", "บางรัก" },
                    { "Bang Khen", "บางเขน" }, { "Bang Kapi", "บางกะปิ" }, { "Pathum Wan", "ปทุมวัน" },
                    { "Pom Prap Sattru Phai", "ป้อมปราบศัตรูพ่าย" },
                    { "Phra Khanong", "พระโขนง" }, { "Min Buri", "มีนบุรี" }, { "Lat Krabang", "ลาดกระบัง" },
                    { "Yan Nawa", "ยานนาวา" },
                    { "Samphanthawong", "สัมพันธวงศ์" }, { "Phaya Thai", "พญาไท" }, { "Thon Buri", "ธนบุรี" },
                    { "Bangkok Yai", "บางกอกใหญ่" },
                    { "Huai Khwang", "ห้วยขวาง" }, { "Khlong San", "คลองสาน" }, { "Taling Chan", "ตลิ่งชัน" },
                    { "Bangkok Noi", "บางกอกน้อย" },
                    { "Bang Khun Thian", "บางขุนเทียน" }, { "Phasi Charoen", "ภาษีเจริญ" }, { "Nong Khaem", "หนองแขม" },
                    { "Rat Burana", "ราษฎร์บูรณะ" },
                    { "Bang Phlat", "บางพลัด" }, { "Din Daeng", "ดินแดง" }, { "Bueng Kum", "บึงกุ่ม" },
                    { "Sathon", "สาทร" },
                    { "Bang Sue", "บางซื่อ" }, { "Chatuchak", "จตุจักร" }, { "Bang Kho Laem", "บางคอแหลม" },
                    { "Prawet", "ประเวศ" },
                    { "Khlong Toei", "คลองเตย" }, { "Suan Luang", "สวนหลวง" }, { "Chom Thong", "จอมทอง" },
                    { "Don Mueang", "ดอนเมือง" },
                    { "Ratchathewi", "ราชเทวี" }, { "Lat Phrao", "ลาดพร้าว" }, { "Watthana", "วัฒนา" },
                    { "Bang Khae", "บางแค" },
                    { "Lak Si", "หลักสี่" }, { "Sai Mai", "สายไหม" }, { "Khan Na Yao", "คันนายาว" },
                    { "Saphan Sung", "สะพานสูง" },
                    { "Wang Thonglang", "วังทองหลาง" }, { "Khlong Sam Wa", "คลองสามวา" }, { "Bang Na", "บางนา" },
                    { "Thawi Watthana", "ทวีวัฒนา" },
                    { "Thung Khru", "ทุ่งครุ" }, { "Bang Bon", "บางบอน" }
            };

            for (String[] d : data) {
                districts.add(District.builder()
                        .city(bangkok)
                        .nameEn(d[0])
                        .nameTh(d[1])
                        .nameMm(d[0])
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
