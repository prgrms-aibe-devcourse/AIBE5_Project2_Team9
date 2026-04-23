package com.pickkasso.pickkasso.global.service;

import com.pickkasso.pickkasso.global.img.DefaultImg;
import com.pickkasso.pickkasso.global.img.DefaultImgDto;
import com.pickkasso.pickkasso.global.img.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DefaultImgService {

    private final S3Service s3Service;

    // 신규 업로드 (등록 시)
    public List<DefaultImgDto> uploadImages(List<MultipartFile> newFiles, List<Integer> newFileOrders, String dirName) throws ImageUploadException {
        if (newFiles == null || newFiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<DefaultImgDto> result = new ArrayList<>();

        for (int i = 0; i < newFiles.size(); i++) {
            try {
                String ext = StringUtils.getFilenameExtension(newFiles.get(i).getOriginalFilename());
                String imgName = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
                String imgUrl = s3Service.upload(newFiles.get(i), dirName, imgName);
                Integer order = newFileOrders != null ? newFileOrders.get(i) : i;
                result.add(new DefaultImgDto(imgUrl, order));
            } catch (IOException e) {
                throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
            }
        }
        return result;
    }


    // 기존은 유지 + new는 upload + deleted는 제거
    public List<DefaultImgDto> updateImages(
        List<? extends DefaultImg> originalImgs,
        List<String> keptImgUrls,
        List<Integer> keptImgOrders,
        List<MultipartFile> newFiles,
        List<Integer> newFileOrders,
        String dirName
    ) {
        List<DefaultImgDto> result = new ArrayList<>();
        Map<String, Boolean> urlMap = new HashMap<>();
        for (DefaultImg img : originalImgs) {
            urlMap.put(img.getImgUrl(), false);
        }

        // 1. 기존 유지분
        if (keptImgUrls != null) {
            for (int i = 0; i < keptImgUrls.size(); i++) {
                Integer order = keptImgOrders != null ? keptImgOrders.get(i) : i;
                result.add(new DefaultImgDto(keptImgUrls.get(i), order));
                urlMap.put(keptImgUrls.get(i), true);
            }
        }

        // 2. 신규 업로드
        if (newFiles != null) {
            for (int i = 0; i < newFiles.size(); i++) {
                try {
                    String ext = StringUtils.getFilenameExtension(newFiles.get(i).getOriginalFilename());
                    String imgName = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
                    String imgUrl = s3Service.upload(newFiles.get(i), dirName, imgName);
                    Integer order = newFileOrders != null ? newFileOrders.get(i) : i;
                    result.add(new DefaultImgDto(imgUrl, order));
                } catch (IOException e) {
                    throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
                }
            }
        }

        // 3. 삭제
        for (String url : urlMap.keySet()) {
            if (urlMap.get(url)) continue;
            s3Service.delete(url);
        }

        // 4. order 기준 정렬
        result.sort(Comparator.comparingInt(DefaultImgDto::getImgOrder));
        return result;
    }
}
