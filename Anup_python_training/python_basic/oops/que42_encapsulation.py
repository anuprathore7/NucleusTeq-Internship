
""" This class stores bank account information securely. """
class Bank:

    def __init__(self,account_holder_name,account_balance) -> None:

        self.account_holder_name = account_holder_name

        self.__account_balance = account_balance


    """ This method displays the account balance. """
    def display_balance(self) -> None:

        print(f"Account Balance: {self.__account_balance}")


    """ This method deposits money into the account. """
    def deposit_amount(self,deposit_amount) -> None:

        self.__account_balance += deposit_amount


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    bank_account = Bank("Anup",5000)

    bank_account.deposit_amount(5000)

    bank_account.display_balance()