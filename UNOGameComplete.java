import java.util.*;

/**
 * 完整的 UNO 游戏 - 所有类集成在一个文件中
 */
public class UNOGameComplete {

    // ==================== Card 类 ====================
    static class Card {
        public enum Color {
            RED, BLUE, GREEN, YELLOW, WILD
        }

        public enum Type {
            NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
        }

        private Color color;
        private Type type;
        private int number; // 仅对数字牌有效

        public Card(Color color, Type type, int number) {
            this.color = color;
            this.type = type;
            this.number = number;
        }

        public Card(Color color, Type type) {
            this(color, type, -1);
        }

        // Getters
        public Color getColor() { return color; }
        public Type getType() { return type; }
        public int getNumber() { return number; }

        // 设置颜色（用于万能牌）
        public void setColor(Color color) {
            if (this.color == Color.WILD) {
                this.color = color;
            }
        }

        // 检查是否可以出这张牌
        public boolean canPlayOn(Card other) {
            if (this.color == Color.WILD) {
                return true;
            }
            if (this.color == other.color) {
                return true;
            }
            if (this.type == other.type && this.type != Type.NUMBER) {
                return true;
            }
            if (this.type == Type.NUMBER && other.type == Type.NUMBER && this.number == other.number) {
                return true;
            }
            return false;
        }

        // 获取卡牌显示名称
        public String getDisplayName() {
            switch (type) {
                case NUMBER:
                    return color.name() + "_" + number;
                case SKIP:
                    return color.name() + "_SKIP";
                case REVERSE:
                    return color.name() + "_REVERSE";
                case DRAW_TWO:
                    return color.name() + "_DRAW2";
                case WILD:
                    return "WILD";
                case WILD_DRAW_FOUR:
                    return "WILD_DRAW4";
                default:
                    return "UNKNOWN";
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    // ==================== Deck 类 ====================
    static class Deck {
        private List<Card> cards;
        private List<Card> discardPile;

        public Deck() {
            cards = new ArrayList<>();
            discardPile = new ArrayList<>();
            initializeDeck();
            shuffle();
        }

        // 初始化牌堆
        private void initializeDeck() {
            // 四种颜色（红、蓝、绿、黄）
            for (Card.Color color : Arrays.asList(Card.Color.RED, Card.Color.BLUE, Card.Color.GREEN, Card.Color.YELLOW)) {
                if (color != Card.Color.WILD) {
                    // 数字牌 0-9
                    for (int i = 0; i <= 9; i++) {
                        if (i == 0) {
                            // 数字0只有一张
                            cards.add(new Card(color, Card.Type.NUMBER, i));
                        } else {
                            // 其他数字有两张
                            cards.add(new Card(color, Card.Type.NUMBER, i));
                            cards.add(new Card(color, Card.Type.NUMBER, i));
                        }
                    }

                    // 功能牌（每种两张）
                    for (int i = 0; i < 2; i++) {
                        cards.add(new Card(color, Card.Type.SKIP));
                        cards.add(new Card(color, Card.Type.REVERSE));
                        cards.add(new Card(color, Card.Type.DRAW_TWO));
                    }
                }
            }

            // 万能牌（每种四张）
            for (int i = 0; i < 4; i++) {
                cards.add(new Card(Card.Color.WILD, Card.Type.WILD));
                cards.add(new Card(Card.Color.WILD, Card.Type.WILD_DRAW_FOUR));
            }
        }

        // 洗牌
        public void shuffle() {
            Collections.shuffle(cards);
        }

        // 抽牌
        public Card drawCard() {
            if (cards.isEmpty()) {
                reshuffleDiscardPile();
            }
            return cards.isEmpty() ? null : cards.remove(cards.size() - 1);
        }

        // 从弃牌堆重新洗牌
        private void reshuffleDiscardPile() {
            if (discardPile.size() > 1) {
                Card topCard = discardPile.remove(discardPile.size() - 1);
                cards.addAll(discardPile);
                discardPile.clear();
                discardPile.add(topCard);
                shuffle();
                System.out.println("牌堆已重新洗牌！");
            }
        }

        // 添加牌到弃牌堆
        public void addToDiscardPile(Card card) {
            discardPile.add(card);
        }

        // 获取弃牌堆顶部的牌
        public Card getTopDiscardCard() {
            return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
        }

        // 获取剩余牌数
        public int getRemainingCards() {
            return cards.size();
        }
    }

    // ==================== Player 类 ====================
    static class Player {
        private String name;
        private List<Card> hand;
        private boolean isAI;

        public Player(String name, boolean isAI) {
            this.name = name;
            this.hand = new ArrayList<>();
            this.isAI = isAI;
        }

        // 添加手牌
        public void addCard(Card card) {
            hand.add(card);
        }

        // 添加多张牌
        public void addCards(List<Card> cards) {
            hand.addAll(cards);
        }

        // 出牌
        public Card playCard(int index) {
            return hand.remove(index);
        }

        // 获取可出的牌
        public List<Integer> getPlayableCards(Card topCard) {
            List<Integer> playable = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).canPlayOn(topCard)) {
                    playable.add(i);
                }
            }
            return playable;
        }

        // AI 选择出牌
        public int chooseCardToPlay(Card topCard) {
            List<Integer> playable = getPlayableCards(topCard);
            if (playable.isEmpty()) {
                return -1; // 需要抽牌
            }

            // 简单AI策略：优先出功能牌，否则随机出
            for (int index : playable) {
                Card card = hand.get(index);
                if (card.getType() != Card.Type.NUMBER) {
                    return index;
                }
            }

            // 没有功能牌，随机出一张可出的牌
            Random random = new Random();
            return playable.get(random.nextInt(playable.size()));
        }

        // AI 选择万能牌颜色
        public Card.Color chooseWildColor() {
            // 统计手牌中各种颜色的数量
            Map<Card.Color, Integer> colorCount = new HashMap<>();
            for (Card card : hand) {
                if (card.getColor() != Card.Color.WILD) {
                    colorCount.put(card.getColor(), colorCount.getOrDefault(card.getColor(), 0) + 1);
                }
            }

            // 选择最多的颜色
            return colorCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(Card.Color.RED);
        }

        // Getters
        public String getName() { return name; }
        public List<Card> getHand() { return hand; }
        public int getHandSize() { return hand.size(); }
        public boolean isAI() { return isAI; }

        // 检查是否只剩一张牌（要喊UNO）
        public boolean hasUno() {
            return hand.size() == 1;
        }

        // 检查是否获胜
        public boolean hasWon() {
            return hand.isEmpty();
        }

        @Override
        public String toString() {
            return name + " (" + hand.size() + "张牌)";
        }
    }

    // ==================== UNO Game 主类 ====================
    private Deck deck;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean directionClockwise; // 游戏方向
    private Scanner scanner;

    public UNOGameComplete() {
        deck = new Deck();
        players = new ArrayList<>();
        scanner = new Scanner(System.in);
        directionClockwise = true;
        initializeGame();
    }

    // 初始化游戏
    private void initializeGame() {
        System.out.println("🎮 欢迎来到 UNO 游戏！");
        System.out.println("==========================================");

        // 设置玩家
        setupPlayers();

        // 初始发牌（每人7张）
        System.out.println("\n发牌中...");
        for (Player player : players) {
            for (int i = 0; i < 7; i++) {
                player.addCard(deck.drawCard());
            }
        }

        // 开始第一张牌（不能是功能牌）
        Card firstCard;
        do {
            firstCard = deck.drawCard();
        } while (firstCard.getType() != Card.Type.NUMBER);

        deck.addToDiscardPile(firstCard);
        System.out.println("起始牌: " + firstCard);
    }

    // 设置玩家
    private void setupPlayers() {
        System.out.print("请输入玩家数量 (1-4): ");
        int playerCount = scanner.nextInt();
        scanner.nextLine(); // 消耗换行符

        for (int i = 1; i <= playerCount; i++) {
            System.out.print("请输入玩家 " + i + " 的名字: ");
            String name = scanner.nextLine();
            players.add(new Player(name, false));
        }

        // 添加AI玩家凑足4人（如果玩家不足4人）
        String[] aiNames = {"电脑1", "电脑2", "电脑3"};
        for (int i = playerCount; i < 4; i++) {
            players.add(new Player(aiNames[i - playerCount], true));
            System.out.println("已添加AI玩家: " + aiNames[i - playerCount]);
        }

        System.out.println("\n游戏玩家列表:");
        for (int i = 0; i < players.size(); i++) {
            System.out.println((i + 1) + ". " + players.get(i).getName() +
                    (players.get(i).isAI() ? " [AI]" : " [玩家]"));
        }
    }

    // 开始游戏
    public void startGame() {
        System.out.println("\n=== UNO 游戏开始！ ===");
        System.out.println("游戏规则说明:");
        System.out.println("- 数字牌: 颜色+数字 (如 RED_5)");
        System.out.println("- 功能牌: SKIP(跳过), REVERSE(反转), DRAW2(抽2张)");
        System.out.println("- 万能牌: WILD(变色), WILD_DRAW4(变色+抽4张)");
        System.out.println("==========================================");

        Random random = new Random();
        currentPlayerIndex = random.nextInt(players.size());
        System.out.println("随机选择起始玩家: " + players.get(currentPlayerIndex).getName());

        while (true) {
            Player currentPlayer = players.get(currentPlayerIndex);
            Card topCard = deck.getTopDiscardCard();

            System.out.println("\n" + "=".repeat(50));
            System.out.println("当前牌: " + topCard);
            System.out.println("当前玩家: " + currentPlayer);
            System.out.println("游戏方向: " + (directionClockwise ? "顺时针" : "逆时针"));
            System.out.println("牌堆剩余: " + deck.getRemainingCards() + "张牌");

            // 处理当前玩家的回合
            if (currentPlayer.isAI()) {
                aiTurn(currentPlayer);
            } else {
                humanTurn(currentPlayer);
            }

            // 检查获胜条件
            if (currentPlayer.hasWon()) {
                System.out.println("\n🎉 " + currentPlayer.getName() + " 获胜了！");
                System.out.println("游戏结束！");
                break;
            }

            // 移动到下一个玩家
            moveToNextPlayer();
        }

        scanner.close();
    }

    // AI 回合
    private void aiTurn(Player player) {
        System.out.println("\n--- " + player.getName() + "的回合 [AI] ---");
        Card topCard = deck.getTopDiscardCard();
        int cardIndex = player.chooseCardToPlay(topCard);

        if (cardIndex == -1) {
            // 需要抽牌
            Card drawnCard = deck.drawCard();
            player.addCard(drawnCard);
            System.out.println(player.getName() + " 抽了一张牌");

            // 抽牌后检查是否能出
            cardIndex = player.chooseCardToPlay(topCard);
            if (cardIndex != -1) {
                playCard(player, cardIndex);
            } else {
                System.out.println(player.getName() + " 选择跳过回合");
            }
        } else {
            playCard(player, cardIndex);
        }

        // 检查是否需要喊UNO
        if (player.hasUno()) {
            System.out.println("🃏 UNO! " + player.getName() + " 只剩一张牌！");
        }
    }

    // 人类玩家回合
    private void humanTurn(Player player) {
        System.out.println("\n--- " + player.getName() + "的回合 [玩家] ---");
        Card topCard = deck.getTopDiscardCard();

        // 显示手牌
        System.out.println("你的手牌:");
        List<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            System.out.println(i + ": " + hand.get(i));
        }

        List<Integer> playableCards = player.getPlayableCards(topCard);
        System.out.println("可出的牌索引: " + playableCards);

        if (playableCards.isEmpty()) {
            System.out.println("❌ 没有可出的牌，必须抽牌！");
            Card drawnCard = deck.drawCard();
            player.addCard(drawnCard);
            System.out.println("你抽到了: " + drawnCard);

            // 检查抽到的牌是否能出
            if (drawnCard.canPlayOn(topCard)) {
                System.out.print("是否要立即出这张牌？(y/n): ");
                String choice = scanner.nextLine();
                if (choice.equalsIgnoreCase("y")) {
                    // 找到刚抽的牌在手中的位置
                    int newCardIndex = hand.indexOf(drawnCard);
                    if (newCardIndex != -1) {
                        playCard(player, newCardIndex);
                    }
                }
            }
        } else {
            System.out.print("请选择要出的牌编号，或输入 -1 抽牌: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 消耗换行符

            if (choice == -1) {
                Card drawnCard = deck.drawCard();
                player.addCard(drawnCard);
                System.out.println("你抽到了: " + drawnCard);
            } else if (choice >= 0 && choice < hand.size() &&
                    playableCards.contains(choice)) {
                playCard(player, choice);
            } else {
                System.out.println("❌ 无效选择，跳过回合");
            }
        }

        // 检查是否需要喊UNO
        if (player.hasUno()) {
            System.out.println("🃏 UNO! " + player.getName() + " 只剩一张牌！");
        }
    }

    // 出牌
    private void playCard(Player player, int cardIndex) {
        Card playedCard = player.playCard(cardIndex);
        deck.addToDiscardPile(playedCard);

        System.out.println("✅ " + player.getName() + " 出了: " + playedCard);

        // 处理万能牌颜色选择
        if (playedCard.getColor() == Card.Color.WILD) {
            if (player.isAI()) {
                Card.Color chosenColor = player.chooseWildColor();
                playedCard.setColor(chosenColor);
                System.out.println(player.getName() + " 选择了颜色: " + chosenColor);
            } else {
                System.out.println("请选择颜色:");
                System.out.println("1: RED(红)  2: BLUE(蓝)  3: GREEN(绿)  4: YELLOW(黄)");
                System.out.print("输入选择 (1-4): ");
                int colorChoice = scanner.nextInt();
                scanner.nextLine(); // 消耗换行符

                Card.Color chosenColor;
                switch (colorChoice) {
                    case 1: chosenColor = Card.Color.RED; break;
                    case 2: chosenColor = Card.Color.BLUE; break;
                    case 3: chosenColor = Card.Color.GREEN; break;
                    case 4: chosenColor = Card.Color.YELLOW; break;
                    default:
                        System.out.println("无效选择，默认选择红色");
                        chosenColor = Card.Color.RED;
                }
                playedCard.setColor(chosenColor);
                System.out.println("已选择颜色: " + chosenColor);
            }
        }

        // 处理特殊牌效果
        handleSpecialCard(playedCard);
    }

    // 处理特殊牌效果
    private void handleSpecialCard(Card card) {
        switch (card.getType()) {
            case SKIP:
                System.out.println("⏭️  跳过下一个玩家！");
                moveToNextPlayer();
                System.out.println("被跳过的玩家: " + players.get(currentPlayerIndex).getName());
                break;

            case REVERSE:
                System.out.println("🔄 反转游戏方向！");
                directionClockwise = !directionClockwise;
                System.out.println("新的方向: " + (directionClockwise ? "顺时针" : "逆时针"));
                break;

            case DRAW_TWO:
                System.out.println("➕ 下家抽2张牌！");
                moveToNextPlayer();
                Player nextPlayer = players.get(currentPlayerIndex);
                nextPlayer.addCard(deck.drawCard());
                nextPlayer.addCard(deck.drawCard());
                System.out.println(nextPlayer.getName() + " 抽了2张牌");
                break;

            case WILD_DRAW_FOUR:
                System.out.println("🎨➕ 下家抽4张牌！");
                moveToNextPlayer();
                Player targetPlayer = players.get(currentPlayerIndex);
                for (int i = 0; i < 4; i++) {
                    targetPlayer.addCard(deck.drawCard());
                }
                System.out.println(targetPlayer.getName() + " 抽了4张牌");
                break;
        }
    }

    // 移动到下一个玩家
    private void moveToNextPlayer() {
        if (directionClockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
    }

    // 主方法
    public static void main(String[] args) {
        try {
            UNOGameComplete game = new UNOGameComplete();
            game.startGame();
        } catch (Exception e) {
            System.out.println("游戏出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}