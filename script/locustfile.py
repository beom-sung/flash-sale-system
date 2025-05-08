import random
import uuid
import time
import json
import requests
import argparse
import sys
from locust import HttpUser, task, events, between

# 테스트 설정
PRODUCTS = ["테스트상품1", "테스트상품2", "아이폰15", "갤럭시S24"]
STOCK_COUNT = 100

# 서버 초기화 및 상품 등록 함수
def initialize_server(host):
    """서버 초기화 및 상품 등록"""
    headers = {"Content-Type": "application/json"}
    
    print("서버 초기화 중...")
    try:
        # 저장소 초기화
        response = requests.post(f"{host}/clear", headers=headers)
        if response.status_code != 200:
            print(f"서버 초기화 실패: {response.status_code}")
            return False
            
        print("서버 초기화 완료")
        
        # 상품 등록
        for product_name in PRODUCTS:
            payload = {
                "productName": product_name,
                "stockCount": STOCK_COUNT
            }
            response = requests.post(f"{host}/products", json=payload, headers=headers)
            if response.status_code != 200:
                print(f"상품 '{product_name}' 등록 실패: {response.status_code}")
                return False
            print(f"상품 '{product_name}' 재고 {STOCK_COUNT}개로 설정 완료")
            
        return True
    except Exception as e:
        print(f"초기화 중 오류 발생: {e}")
        return False

# 재고 상태 확인 함수 
def check_products(host):
    """등록된 상품의 재고 상태를 확인"""
    print("\n=== 상품 재고 상태 확인 ===")
    try:
        # 서버에 GET /products API가 없으므로, 상품 정보만 출력
        print("현재 등록된 상품 목록:")
        for product_name in PRODUCTS:
            print(f"- {product_name}")
        print("\n참고: 현재 서버 API에서는 상품별 실시간 재고를 조회할 수 없습니다.")
        print("재고를 확인하려면 테스트 실행 후 주문 수를 확인하세요.")
    except Exception as e:
        print(f"상품 조회 중 오류 발생: {e}")

# 주문 현황 확인 함수
def check_orders(host):
    """상품별 주문 현황 확인"""
    print("\n=== 상품별 주문 현황 ===")
    try:
        for product_name in PRODUCTS:
            # URL 인코딩 처리
            import urllib.parse
            encoded_product = urllib.parse.quote(product_name)
            response = requests.get(f"{host}/orders?productName={encoded_product}")
            if response.status_code == 200:
                orders = response.json()
                print(f"상품 '{product_name}': {orders} 주문")
            else:
                print(f"상품 '{product_name}' 조회 실패: {response.status_code}")
    except Exception as e:
        print(f"결과 조회 중 오류 발생: {e}")

# 직접 실행 가능한 초기화 스크립트 기능
def run_as_script():
    """이 파일을 직접 실행할 때 사용할 초기화 스크립트 기능"""
    parser = argparse.ArgumentParser(description='Flash Sale 테스트 초기화 및 상태 확인')
    parser.add_argument('--host', type=str, default='http://localhost:8080', help='서버 호스트 주소')
    parser.add_argument('--init', action='store_true', help='서버 초기화 및 상품 등록')
    parser.add_argument('--check', action='store_true', help='상품 상태 확인')
    parser.add_argument('--orders', action='store_true', help='주문 현황 확인')
    
    args = parser.parse_args()
    
    if args.init:
        initialize_server(args.host)
    
    if args.check:
        check_products(args.host)
    
    if args.orders:
        check_orders(args.host)
    
    # 아무 옵션도 지정하지 않았을 경우 도움말 표시
    if not (args.init or args.check or args.orders):
        parser.print_help()

# Locust 시작 시 서버 초기화를 수행하는 이벤트 핸들러
@events.init_command_line_parser.add_listener
def on_init_command_line_parser(parser):
    parser.add_argument("--skip-init", action="store_true", help="Skip server initialization")

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    if not environment.parsed_options.skip_init:
        if not initialize_server(environment.host):
            print("서버 초기화 실패. 테스트를 중단합니다.")
            environment.runner.quit()

# 테스트 종료 시 주문 결과 확인
@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    print("\n=== 테스트 종료 - 각 상품별 주문 수 확인 ===")
    try:
        for product_name in PRODUCTS:
            # URL 인코딩 처리
            import urllib.parse
            encoded_product = urllib.parse.quote(product_name)
            response = requests.get(f"{environment.host}/orders?productName={encoded_product}")
            if response.status_code == 200:
                orders = response.json()
                print(f"상품 '{product_name}': {orders} 주문 성공")
            else:
                print(f"상품 '{product_name}' 조회 실패: {response.status_code}")
    except Exception as e:
        print(f"결과 조회 중 오류 발생: {e}")

class FlashSaleUser(HttpUser):
    """선착순 이벤트 사용자 시뮬레이션"""
    # 사용자별 0.5~2초 간격으로 요청 (실제 사용자 행동 시뮬레이션)
    wait_time = between(0.5, 2)
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # 사용자 ID 생성
        self.user_id = f"user_{uuid.uuid4().hex[:8]}"
    
    @task
    def order_product(self):
        """무작위 상품 주문"""
        # 무작위 상품 선택
        product_name = random.choice(PRODUCTS)
        
        # 주문 요청
        payload = {
            "uuid": self.user_id,
            "productName": product_name
        }
        
        with self.client.post(
            "/orders",
            json=payload,
            headers={"Content-Type": "application/json"},
            name=f"주문: {product_name}",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                result = response.json()
                # 주문 결과와 관계없이 항상 성공으로 처리
                # 재고 부족으로 인한 주문 실패도 정상적인 동작으로 간주
                response.success()
                # 로그 메시지만 다르게 표시
                if result.get("success", False):
                    # 성공한 주문
                    pass
                else:
                    # 서버에서 처리된 실패 (예: 재고 부족)
                    # 로깅만 하고 실패로 취급하지 않음
                    pass
            else:
                # HTTP 오류는 여전히 실패로 처리
                response.failure(f"HTTP 오류: {response.status_code}")

# 직접 실행 여부 확인
if __name__ == "__main__":
    # sys.argv를 확인하여 Locust가 실행한 것인지 직접 실행한 것인지 판단
    if len(sys.argv) > 1 and sys.argv[1] in ['-f', '--locustfile']:
        # Locust가 실행한 경우
        pass
    else:
        # 스크립트로 직접 실행한 경우
        run_as_script()
        # Locust 실행이 아닌 경우 종료
        sys.exit(0)

# 주의: Locust로 실행하려면 'locust -f locustfile.py --host=http://localhost:8080'로 실행하세요.
# 초기화/상태 확인만 하려면 'python3 locustfile.py --init --check --host=http://localhost:8080'로 실행하세요. 