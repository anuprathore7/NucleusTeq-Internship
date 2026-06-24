import os
from motor.motor_asyncio import AsyncIOMotorClient
from dotenv import load_dotenv
"""It loads the dotenv files variables """
load_dotenv()

MONGO_DB_URI = os.getenv("MONGO_URI" , "mongodb://localhost:27017")
MONGO_DB_NAME = os.getenv("DB_NAME" , "assessment_portal")

client = AsyncIOMotorClient(MONGO_DB_URI)
database= client[MONGO_DB_NAME]

