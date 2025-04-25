#!/usr/bin/env python3
import requests
import concurrent.futures
import uuid
import time
import random

# 상수 설정
USER_COUNT = 1000          # 사용자 수
ORDER_COUNT = USER_COUNT * 10        # 주문 요청 수
PRODUCT_NAMES = ["테스트상품1", "테스트상품2", "아이폰15", "갤럭시S24"]
STOCK_COUNT = 100          # 각 상품의 초기 재고 수량
MAX_WORKERS = 100         # 동시 실행할 최대 스레드 수

def clear_repository():
    """서버의 저장소를 초기화하는 API를 호출합니다."""
    url = "http://localhost:8080/clear"
    headers = {"Content-Type": "application/json"}
    
    try:
        response = requests.post(url, headers=headers)
        return {
            "status_code": response.status_code,
            "response": response.text
        }
    except Exception as e:
        return {
            "status_code": None,
            "response": str(e)
        }

def set_product_stock(product_name, stock_count):
    """상품 재고를 설정하는 API를 호출합니다."""
    url = "http://localhost:8080/products"
    headers = {"Content-Type": "application/json"}
    payload = {
        "productName": product_name,
        "stockCount": stock_count
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        return {
            "product_name": product_name,
            "status_code": response.status_code,
            "response": response.json() if response.status_code == 200 else response.text
        }
    except Exception as e:
        return {
            "product_name": product_name,
            "status_code": None,
            "response": str(e)
        }

def generate_uuids(count):
    """지정된 개수만큼 UUID를 생성합니다."""
    return [f"user_{uuid.uuid4().hex[:8]}" for _ in range(count)]

def create_order(user_uuid, product_name):
    """주문 API를 호출합니다."""
    url = "http://localhost:8080/orders"
    headers = {"Content-Type": "application/json"}
    payload = {
        "uuid": user_uuid,
        "productName": product_name
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response_data = response.json() if response.status_code == 200 else None
        is_success = response_data.get('success', False) if response_data else False
        
        return {
            "user_uuid": user_uuid,
            "product_name": product_name,
            "status_code": response.status_code,
            "success": is_success,
            "response": response_data if response_data else response.text
        }
    except Exception as e:
        return {
            "user_uuid": user_uuid,
            "product_name": product_name,
            "status_code": None,
            "success": False,
            "response": str(e)
        }

def get_order_count(product_name):
    """특정 상품의 주문 성공 횟수를 조회합니다."""
    url = f"http://localhost:8080/orders?productName={product_name}"
    headers = {"Content-Type": "application/json"}
    
    try:
        response = requests.get(url, headers=headers)
        return {
            "product_name": product_name,
            "count": response.json() if response.status_code == 200 else response.text
        }
    except Exception as e:
        return {
            "product_name": product_name,
            "count": str(e)
        }

def setup_products():
    """저장소를 초기화하고 상품 재고를 설정합니다."""
    # 저장소 초기화
    print("서버 저장소를 초기화합니다...")
    clear_result = clear_repository()
    print(f"저장소 초기화: {'성공' if clear_result['status_code'] == 200 else '실패'}")
    
    # 상품 재고 설정
    print("\n상품 재고를 설정합니다...")
    for product_name in PRODUCT_NAMES:
        result = set_product_stock(product_name, STOCK_COUNT)
    
    print("\n상품 설정이 완료되었습니다. 설정을 확인한 후 테스트를 시작하려면 Enter 키를 누르세요...")

def run_test():
    """동시성 테스트를 실행합니다."""
    # UUID 생성
    uuids = generate_uuids(USER_COUNT)
    print(f"\n테스트에 사용할 {USER_COUNT}명의 사용자가 생성되었습니다.")
    print(f"총 {ORDER_COUNT}개의 주문 요청을 전송합니다.")
    
    # 동시에 주문 API 요청
    print("동시에 주문 API 요청을 시작합니다...")
    start_time = time.time()
    
    # 요청할 주문 목록 생성 (랜덤 사용자, 랜덤 상품)
    order_requests = []
    for _ in range(ORDER_COUNT):
        user_uuid = random.choice(uuids)
        product_name = random.choice(PRODUCT_NAMES)
        order_requests.append((user_uuid, product_name))
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        # 각 주문 요청을 제출
        future_to_order = {}
        for user_uuid, product_name in order_requests:
            future = executor.submit(create_order, user_uuid, product_name)
            future_to_order[future] = (user_uuid, product_name)
        
        results = []
        
        # 모든 요청의 응답을 기다림
        for future in concurrent.futures.as_completed(future_to_order):
            results.append(future.result())
    
    end_time = time.time()
    print(f"모든 요청 완료: {end_time - start_time:.2f}초 소요됨")
    
    # 요청 결과 요약
    success_count = sum(1 for r in results if r["status_code"] == 200)
    print(f"성공 응답: {success_count}/{ORDER_COUNT}")
    
    # 상품별 요청 및 성공 요약
    product_stats = {}
    for r in results:
        product_name = r["product_name"]
        if product_name not in product_stats:
            product_stats[product_name] = {"total": 0, "success_true": 0, "success_false": 0}
        
        product_stats[product_name]["total"] += 1
        
        if r["status_code"] == 200:
            if r["success"]:
                product_stats[product_name]["success_true"] += 1
            else:
                product_stats[product_name]["success_false"] += 1
    
    print("\n상품별 요청 통계:")
    for product, stats in product_stats.items():
        print(f"- {product}: 성공: {stats['success_true']} | 실패: {stats['success_false']} | 총 요청: {stats['total']}")
    
    # DB에 데이터가 적재될 때까지 3초 대기
    print("\nDB에 데이터가 적재될 때까지 3초 대기 중...")
    time.sleep(3)
    
    # 각 상품별 주문 성공 횟수 조회
    print("\n서버에서 각 상품별 주문 성공 횟수를 조회합니다...")
    for product_name in PRODUCT_NAMES:
        order_count_result = get_order_count(product_name)
        print(f"상품 '{product_name}' 주문 성공 횟수: {order_count_result['count']}")

def main():
    # 1단계: 상품 설정
    setup_products()
    
    # 사용자 입력 대기
    input()
    
    # 2단계: 테스트 실행
    run_test()

if __name__ == "__main__":
    main() 