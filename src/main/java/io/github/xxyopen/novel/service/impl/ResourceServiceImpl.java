package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.dto.resp.ImgVerifyCodeRespDto;
import io.github.xxyopen.novel.manager.redis.VerifyCodeManager;
import io.github.xxyopen.novel.service.ResourceService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源（图片/视频/文档）相关服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final VerifyCodeManager verifyCodeManager;

    @Value("${novel.file.upload.path}")
    private String fileUploadPath;

    @Override
    public RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException {
        String sessionId = IdWorker.get32UUID();
        return RestResp.ok(ImgVerifyCodeRespDto.builder()
            .sessionId(sessionId)
            .img(verifyCodeManager.genImgVerifyCode(sessionId))
            .build());
    }

    @SneakyThrows
    @Override
    public RestResp<String> uploadImage(MultipartFile file) {
        LocalDateTime now = LocalDateTime.now();
        String savePath =
            SystemConfigConsts.IMAGE_UPLOAD_DIRECTORY
                + now.format(DateTimeFormatter.ofPattern("yyyy")) + File.separator
                + now.format(DateTimeFormatter.ofPattern("MM")) + File.separator
                + now.format(DateTimeFormatter.ofPattern("dd"));
        String oriName = file.getOriginalFilename();
        assert oriName != null;
        String saveFileName = IdWorker.get32UUID() + oriName.substring(oriName.lastIndexOf("."));
        /*
        saveFile是相对路径，指向 upload/doc20170816162034_001.jpg
        file.transferTo 方法调用时，判断如果是相对路径，则使用temp目录，为父目录
        因此，实际保存位置为 C:\Users\AppData\Local\Temp\doc20170816162034_001.jpg
        
        一则，位置不对，二则没有父目录存在，因此产生上述错误。
         */
        File saveFile = new File(new File(savePath).getAbsolutePath()+"/"+ saveFileName);
        if (!saveFile.getParentFile().exists()) {
            boolean isSuccess = saveFile.getParentFile().mkdirs();
            if (!isSuccess) {
                throw new BusinessException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR);
            }
        }
        file.transferTo(saveFile);
        if (Objects.isNull(ImageIO.read(saveFile))) {
            // 上传的文件不是图片
            Files.delete(saveFile.toPath());
            throw new BusinessException(ErrorCodeEnum.USER_UPLOAD_FILE_TYPE_NOT_MATCH);
        }
        return RestResp.ok(savePath + File.separator + saveFileName);
    }

}
