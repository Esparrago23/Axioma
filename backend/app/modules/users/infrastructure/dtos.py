from typing import Optional
from pydantic import BaseModel, Field, model_validator
from datetime import datetime

class UserUpdateDTO(BaseModel):
    username: Optional[str] = None
    full_name: Optional[str] = None
    profile_picture_url: Optional[str] = None

class UserResponseDTO(BaseModel):
    id: int
    username: str
    email: str
    reputation_score: int
    full_name: Optional[str] = None
    profile_picture_url: Optional[str] = None
    created_at: datetime


class UpdateFcmTokenDTO(BaseModel):
    fcm_token: Optional[str] = Field(default=None, min_length=1)
    last_latitude: Optional[float] = None
    last_longitude: Optional[float] = None

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "fcm_token": "eXampleFcmToken123",
                    "last_latitude": -34.6037,
                    "last_longitude": -58.3816,
                },
                {
                    "last_latitude": -34.6037,
                    "last_longitude": -58.3816,
                },
                {
                    "fcm_token": None,
                },
            ]
        }
    }

    @model_validator(mode="after")
    def validate_location_pair(self):
        if (self.last_latitude is None) != (self.last_longitude is None):
            raise ValueError("last_latitude y last_longitude deben enviarse juntos")
        return self

    @property
    def has_fcm_token(self) -> bool:
        return "fcm_token" in self.model_fields_set

    @property
    def has_location(self) -> bool:
        return "last_latitude" in self.model_fields_set or "last_longitude" in self.model_fields_set
