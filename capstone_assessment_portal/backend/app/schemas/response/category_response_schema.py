from datetime import datetime
from pydantic import BaseModel


class CategoryResponseSchema(BaseModel):
    """
    Response schema for a single category.
    This is what we send back to the client — clean and controlled.
    We never send raw MongoDB documents directly to the client.
    
    """

    id: str                     # MongoDB _id converted to string
    name: str                   # category name
    description: str            # category description
    is_active: bool             # whether category is active
    created_at: datetime        # when it was created
    updated_at: datetime        # when it was last updated


class CategoryListResponseSchema(BaseModel):
    """
    Response schema when returning multiple categories.
    Wraps the list with a count so clients know
    how many results came back without counting manually.
    """

    total: int                          # total number of categories
    categories: list[CategoryResponseSchema]   # the actual list