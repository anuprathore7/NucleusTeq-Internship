"""It is user response schema it will return to the frontend"""

from datetime import datetime
from pydantic import BaseModel, EmailStr

class UserResponseSchema(BaseModel):
    id : str
    username: str
    email:EmailStr
    role : str
    is_active : bool
    created_at : datetime
