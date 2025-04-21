package pe.edu.upeu.sysalmacen.control;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.sysalmacen.dtos.report.ProdMasVendidosDTO;
import pe.edu.upeu.sysalmacen.excepciones.FileProcessingException;
import pe.edu.upeu.sysalmacen.excepciones.ReportGenerationException;
import pe.edu.upeu.sysalmacen.modelo.MediaFile;
import pe.edu.upeu.sysalmacen.servicio.IMediaFileService;
import pe.edu.upeu.sysalmacen.servicio.IProductoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reporte")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    
    private final IProductoService productoService;
    private final IMediaFileService mfService;
    private final Cloudinary cloudinary;

    @GetMapping("/pmvendidos")
    public ResponseEntity<List<ProdMasVendidosDTO>> getProductosMasVendidos() {
        List<ProdMasVendidosDTO> productos = productoService.obtenerProductosMasVendidos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping(value = "/generateReport", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateReport() {
        try {
            byte[] data = productoService.generateReport();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            String errorMsg = "Error al generar el reporte: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new ReportGenerationException(errorMsg, e);
        }
    }

    @GetMapping(value = "/readFile/{idFile}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> readFile(@PathVariable("idFile") Long idFile) {
        try {
            Optional<MediaFile> mediaFileOpt = Optional.ofNullable(mfService.findById(idFile));
            MediaFile mediaFile = mediaFileOpt.orElseThrow(() -> 
                new FileProcessingException("Archivo no encontrado con ID: " + idFile));
            
            if (mediaFile.getContent() == null) {
                throw new FileProcessingException("El archivo con ID " + idFile + " no tiene contenido");
            }
            
            return ResponseEntity.ok(mediaFile.getContent());
        } catch (Exception e) {
            String errorMsg = String.format("Error al leer el archivo con ID %d: %s", idFile, e.getMessage());
            logger.error(errorMsg, e);
            throw new FileProcessingException(errorMsg, e);
        }
    }

    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            validateMultipartFile(multipartFile);
            
            MediaFile mf = new MediaFile();
            mf.setContent(multipartFile.getBytes());
            mf.setFileName(multipartFile.getOriginalFilename());
            mf.setFileType(multipartFile.getContentType());
            mfService.save(mf);
            
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            String errorMsg = "Error al guardar el archivo: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new FileProcessingException(errorMsg, e);
        }
    }

    @PostMapping(value = "/saveFileCloud", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFileCloud(@RequestParam("file") MultipartFile multipartFile) {
        Path tempFilePath = null;
        try {
            validateMultipartFile(multipartFile);
            tempFilePath = convertToFile(multipartFile).toPath();
            
            Map<String, Object> response = cloudinary.uploader().upload(
                tempFilePath.toFile(), 
                ObjectUtils.asMap("resource_type", "auto")
            );
            
            JSONObject json = new JSONObject(response);
            String url = json.getString("url");
            logger.info("Archivo subido a Cloudinary. URL: {}", url);
            
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            String errorMsg = "Error al subir archivo a Cloudinary: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new FileProcessingException(errorMsg, e);
        } finally {
            if (tempFilePath != null) {
                deleteTempFile(tempFilePath);
            }
        }
    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("El archivo no puede estar vacío");
        }
    }

    private void deleteTempFile(Path filePath) {
        try {
            Files.delete(filePath);
            logger.debug("Archivo temporal eliminado: {}", filePath);
        } catch (IOException e) {
            logger.warn("No se pudo eliminar el archivo temporal {}: {}", filePath, e.getMessage());
        }
    }
}