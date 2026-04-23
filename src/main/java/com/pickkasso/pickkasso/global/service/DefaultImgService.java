package com.pickkasso.pickkasso.global.service;

import com.pickkasso.pickkasso.global.img.DefaultImg;
import com.pickkasso.pickkasso.global.img.DefaultImgDto;
import com.pickkasso.pickkasso.global.img.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                String imgName = UUID.randomUUID().toString() + "." + ext;
                String imgUrl = s3Service.upload(newFiles.get(i), dirName, imgName);
                Integer order = newFileOrders != null ? newFileOrders.get(i) : i;
                result.add(new DefaultImgDto(imgName, imgUrl, order));
            } catch (IOException e) {
                throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
            }
        }
        return result;
    }


    // 기존은 유지 + new는 upload + deleted는 제거
    public List<String> updateImages(
        List<DefaultImgDto> oldList,
        List<DefaultImgDto> newList,
        List<MultipartFile> newFiles
    ) {
        // old 중 kept
        return null;
    }
}
