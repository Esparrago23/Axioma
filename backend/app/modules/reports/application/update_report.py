from fastapi import HTTPException
from typing import Optional  # <--- ESTE ES EL QUE FALTABA
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.infrastructure.dtos import UpdateReportDTO
from app.core.storage.repository import StorageRepository 

class UpdateReportUseCase:
    def __init__(self, repo: ReportRepository, storage_repo: StorageRepository):
        self.repo = repo
        self.storage_repo = storage_repo

    def execute(self, rid: int, uid: int, dto: UpdateReportDTO):
        # 1. Buscamos el reporte original y verificamos dueño
        report = self.repo.get_by_id(rid)
        if not report:
            raise HTTPException(404, "Reporte no encontrado")
        
        if report.user_id != uid:
            raise HTTPException(403, "No tienes permiso")

        # 2. Identificamos si habrá cambio de imagen para limpiar el storage
        old_photo_url = report.photo_url 
        
        # 3. Actualizamos los datos del reporte
        report.title = dto.title
        report.description = dto.description
        
        # 4. Actualizamos el campo de la imagen (null para borrar, string para cambiar)
        report.photo_url = dto.photo_url 

        # 5. Guardamos en la DB
        updated_report = self.repo.save(report)

        # 6. Lógica de limpieza física en MinIO/S3
        # Si la URL cambió o se puso en null, borramos el archivo viejo
        if old_photo_url and (old_photo_url != dto.photo_url):
            try:
                object_name = self._extract_object_name(old_photo_url)
                if object_name:
                    self.storage_repo.delete_file(object_name)
                    print(f"✅ Archivo viejo eliminado del storage: {object_name}")
            except Exception as e:
                # No detenemos la respuesta si el borrado físico falla, solo logueamos
                print(f"⚠️ No se pudo borrar el archivo físico: {e}")

        return updated_report

    def _extract_object_name(self, url: str) -> Optional[str]:
        """Saca el path relativo 'report_pictures/...' de la URL completa."""
        if not url:
            return None
        # Ajusta esto según el nombre de tu bucket
        if "/axioma-bucket/" in url:
            return url.split("/axioma-bucket/")[-1]
        return None