import requests

PRODUCT_NAMES = ["테스트상품1", "테스트상품2", "아이폰15", "갤럭시S24"]
STOCK_COUNT = 100

def clear_repository():
    url = "http://localhost:8080/clear"
    headers = {"Content-Type": "application/json"}
    try:
        response = requests.post(url, headers=headers)
        print(f"저장소 초기화: {response.text}")
    except Exception as e:
        print(f"저장소 초기화 실패: {e}")

def set_product_stock(product_name, stock_count):
    url = "http://localhost:8080/products"
    headers = {"Content-Type": "application/json"}
    payload = {
        "productName": product_name,
        "stockCount": stock_count
    }
    try:
        response = requests.post(url, json=payload, headers=headers)
        print(f"상품 '{product_name}' 재고 {stock_count}개로 설정: {response.text}")
    except Exception as e:
        print(f"상품 '{product_name}' 재고 설정 실패: {e}")

def main():
    print("서버 저장소를 초기화합니다...")
    clear_repository()
    print("상품 재고를 설정합니다...")
    for product_name in PRODUCT_NAMES:
        set_product_stock(product_name, STOCK_COUNT)
    print("상품 등록 완료!")

if __name__ == "__main__":
    main() 