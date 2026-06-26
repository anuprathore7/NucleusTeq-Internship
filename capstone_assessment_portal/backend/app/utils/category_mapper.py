from app.schemas.response.category_response_schema import (
    CategoryResponseSchema,
    CategoryListResponseSchema
)


def category_to_response(category: dict) -> CategoryResponseSchema:
    """
    Converts a raw MongoDB category document into a clean response schema.
    """
    return CategoryResponseSchema(
        id=str(category["_id"]),        # ObjectId → string
        name=category["name"],
        description=category["description"],
        is_active=category["is_active"],
        created_at=category["created_at"],
        updated_at=category["updated_at"]
    )


def categories_to_response(categories: list[dict]) -> CategoryListResponseSchema:
    """
    Converts a list of raw MongoDB category documents
    into a paginated list response.
    """
    return CategoryListResponseSchema(
        total=len(categories),
        categories=[category_to_response(cate) for cate in categories]
        # list comprehension = loop through each doc and convert it
    )