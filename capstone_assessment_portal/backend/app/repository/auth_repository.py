"""
Repository layer = ONLY database operations, zero business logic.
Think of it as a translator between Python and MongoDB.
"""

from bson import ObjectId
# ObjectId is MongoDB's unique ID type — like an auto-increment ID in SQL


from app.config.database import database


class AuthRepository:

    def __init__(self):
        # Point to the 'users' collection inside our database
        # MongoDB creates this collection automatically on first insert
        self.collection = database["users"]

    async def find_user_by_email(self, email: str) -> dict | None:
        """
        Search for one user matching the given email.
        Returns the full user document (dict) or None if not found.
        find_one is like: SELECT * FROM users WHERE email = ? LIMIT 1
        """
        result = await self.collection.find_one({"email": email})
        return result

    async def find_user_by_username(self, username: str) -> dict | None:
        """
        Search for one user matching the given username.
        """
        result = await self.collection.find_one({"username": username})
        return result

    async def find_user_by_id(self, user_id: str) -> dict | None:
        """
        Search for one user by their MongoDB _id.
        We convert the string user_id to ObjectId because
        MongoDB stores IDs as ObjectId, not plain strings.
        """
        result = await self.collection.find_one({"_id": ObjectId(user_id)})
        return result

    async def create_user(self, user_data: dict) -> dict:
        """
        Insert a new user document into the collection.
        After inserting, we re-fetch the document so we can return
        the complete saved version including the auto-generated _id.
        """
        # insert_one saves the document and returns an InsertOneResult object
        inserted = await self.collection.insert_one(user_data)

        # result.inserted_id is the ObjectId that MongoDB assigned
        # We use it to fetch back the complete saved document
        result = await self.collection.find_one(
            {"_id": inserted.inserted_id}
        )
        return result