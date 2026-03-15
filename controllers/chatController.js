const axios = require("axios");
const Product = require("../models/Product");

const apiKey = "FORBIDDEN_KEY";
const openaiUrl = "https://openai.taiduc1001.net/v1/chat/completions";

// Simple in-memory storage mapped by user ID
const chatHistories = new Map();

// Generate connection prompt injected with top product context
const buildSystemPrompt = async () => {
    let promptStr = `You are a helpful tech store assistant for 'TechShop'. Help users find electronic products based on their preferences.
Keep responses concise, friendly, and structured. Recommend 1-3 products based on user needs.
If they want something specific, guide them to the right product from the list below.
Always include the product price and format currency as VND (₫) nicely when recommending.\n\nAvailable Products from our Catalog:\n`;

    try {
        // Fetching top 30 active products for context injection
        const products = await Product.find({ status: "ACTIVE" }).limit(30).lean();
        if (products.length > 0) {
            products.forEach(p => {
                promptStr += `- ${p.name} (Category: ${p.category}) - Price: ${p.price}₫. Features: ${p.description || "N/A"}\n`;
            });
        } else {
             promptStr += "No products are currently available in the catalog.\n";
        }
    } catch(err) {
        console.error("Failed to fetch products for LLM context", err);
    }
    return promptStr;
};

// Retrieve history for specific user session
const getChatHistoryStr = (userId) => {
    let history = chatHistories.get(userId.toString());
    if (!history) {
        history = [];
        chatHistories.set(userId.toString(), history);
    }
    return history;
};

// GET /api/chat/history
const getHistory = async (req, res) => {
    try {
        const userId = req.user._id;
        const history = getChatHistoryStr(userId);
        res.json(history);
    } catch(err) {
       res.status(500).json({ message: "Server error", error: err.message });
    }
}

// POST /api/chat/clear
const clearHistory = async (req, res) => {
    try {
        const userId = req.user._id;
        chatHistories.delete(userId.toString());
        res.json({ status: "cleared" });
    } catch(err) {
       res.status(500).json({ message: "Server error", error: err.message });
    }
}

// POST /api/chat
const chat = async (req, res) => {
    try {
        const { message } = req.body;
        if (!message || message.trim() === "") {
            return res.status(400).json({ error: "Message is required" });
        }

        const userId = req.user._id;
        const history = getChatHistoryStr(userId);
        
        // Append user query to memory
        history.push({ role: "user", content: message });

        // Retrieve AI completion
        const botResponse = await callOpenAI(history);
        
        // Append AI response to memory
        history.push({ role: "assistant", content: botResponse });

        // Memory Window limit: Retain last 20 messages to prevent token bloat
        if (history.length > 20) {
            const shortened = history.slice(history.length - 20);
            chatHistories.set(userId.toString(), shortened);
        }

        res.json({ response: botResponse });
    } catch (err) {
        console.error("Chat Error: ", err);
        res.status(500).json({ error: "Failed to process chat: " + err.message });
    }
};

// Internal OpenAI API Request mapping
const callOpenAI = async (history) => {
    try {
        const systemPrompt = await buildSystemPrompt();
        const messages = [{ role: "system", content: systemPrompt }];
        
        history.forEach(msg => {
            messages.push({
                role: msg.role === "user" ? "user" : "assistant",
                content: msg.content
            });
        });

        const payload = {
            model: "gpt-4o-mini",
            messages: messages,
            max_tokens: 500
        };

        const response = await axios.post(openaiUrl, payload, {
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${apiKey}`
            }
        });

        if (response.data && response.data.choices && response.data.choices.length > 0) {
            return response.data.choices[0].message.content;
        }
        return "Sorry, I couldn't formulate a proper response at this time.";

    } catch (error) {
        console.error("OpenAI call error:", error?.response?.data || error.message);
        return "I'm sorry, my communication lines to OpenAI are down. Please try again later.";
    }
}

module.exports = {
   getHistory,
   clearHistory,
   chat
};
