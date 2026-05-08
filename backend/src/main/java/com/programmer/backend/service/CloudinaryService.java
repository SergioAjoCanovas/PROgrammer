package com.programmer.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("cloud_name", "dmfsfky9r"); // Tu Cloud Name
        valuesMap.put("api_key", "856664266132363");   // Pega tu API Key aquí
        valuesMap.put("api_secret", "RLm0bg2FlZW5-Vy3IpVx6_icR3Q"); // Pega tu API Secret aquí
        cloudinary = new Cloudinary(valuesMap);
    }

    public String subirImagen(MultipartFile archivo) throws IOException {
        // Subimos los bytes directamente a la nube, sin guardar nada en disco
        Map resultado = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.emptyMap());
        // Devolvemos la URL segura (https) donde se ha guardado la foto
        return resultado.get("secure_url").toString();
    }
}