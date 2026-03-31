import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User } from 'lucide-react';
import { Button } from '../components/Button';
import { Alert } from '../components/Alert';
import { api } from '../services/mockApi';

interface Message {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

export function AIAssistant() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const suggestedQuestions = [
    "Which vehicle has the highest maintenance cost this month?",
    "Summarise overall fleet performance.",
    "Which drivers are currently available?",
    "Are any vehicles overdue for maintenance?",
    "Suggest the best vehicle for a long-distance trip.",
  ];

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async (messageText?: string) => {
    const text = messageText || input.trim();
    if (!text) return;

    const userMessage: Message = {
      id: Date.now(),
      role: 'user',
      content: text,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setError('');
    setLoading(true);

    try {
      const response = await api.sendAiMessage(text);
      
      const aiMessage: Message = {
        id: Date.now() + 1,
        role: 'assistant',
        content: response,
        timestamp: new Date(),
      };

      setMessages(prev => [...prev, aiMessage]);
    } catch (err) {
      setError('AI service is temporarily unavailable. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="h-[calc(100vh-4rem)] flex flex-col bg-white rounded-lg shadow-sm border border-[#E2E8F0] overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#0F172A] to-[#1E293B] px-6 py-4 border-b border-[#E2E8F0]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-[#3B82F6] rounded-lg flex items-center justify-center">
            <Bot className="h-6 w-6 text-white" />
          </div>
          <div>
            <h2 className="text-lg font-semibold text-white">FleetSync AI</h2>
            <p className="text-sm text-[#94A3B8]">Powered by OpenAI — Ask anything about your fleet</p>
          </div>
        </div>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="p-4">
          <Alert variant="warning">{error}</Alert>
        </div>
      )}

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {messages.length === 0 ? (
          <div className="max-w-2xl mx-auto text-center py-12">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Bot className="h-8 w-8 text-[#3B82F6]" />
            </div>
            <h3 className="text-xl font-semibold text-[#1E293B] mb-2">Welcome to FleetSync AI</h3>
            <p className="text-[#64748B] mb-6">
              Ask me anything about your fleet. I have access to your live data including vehicles, drivers, trips, and maintenance records.
            </p>
            
            <div className="space-y-2">
              <p className="text-sm font-medium text-[#64748B] mb-3">Try asking:</p>
              {suggestedQuestions.map((question, index) => (
                <button
                  key={index}
                  onClick={() => handleSend(question)}
                  className="block w-full text-left px-4 py-3 bg-[#F8FAFC] hover:bg-[#F1F5F9] rounded-lg text-sm text-[#475569] transition-colors"
                >
                  {question}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <>
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex gap-3 ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {message.role === 'assistant' && (
                  <div className="w-8 h-8 bg-[#0F172A] rounded-lg flex items-center justify-center flex-shrink-0">
                    <Bot className="h-5 w-5 text-white" />
                  </div>
                )}
                
                <div className={`max-w-2xl ${message.role === 'user' ? 'order-first' : ''}`}>
                  <div
                    className={`rounded-lg px-4 py-3 ${
                      message.role === 'user'
                        ? 'bg-[#3B82F6] text-white'
                        : 'bg-[#F1F5F9] text-[#1E293B]'
                    }`}
                  >
                    <p className="text-sm whitespace-pre-wrap">{message.content}</p>
                  </div>
                  <p className="text-xs text-[#94A3B8] mt-1 px-1">
                    {message.timestamp.toLocaleTimeString()}
                  </p>
                </div>

                {message.role === 'user' && (
                  <div className="w-8 h-8 bg-[#3B82F6] rounded-lg flex items-center justify-center flex-shrink-0">
                    <User className="h-5 w-5 text-white" />
                  </div>
                )}
              </div>
            ))}
            
            {loading && (
              <div className="flex gap-3">
                <div className="w-8 h-8 bg-[#0F172A] rounded-lg flex items-center justify-center flex-shrink-0">
                  <Bot className="h-5 w-5 text-white" />
                </div>
                <div className="bg-[#F1F5F9] rounded-lg px-4 py-3">
                  <div className="flex gap-1">
                    <div className="w-2 h-2 bg-[#64748B] rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                    <div className="w-2 h-2 bg-[#64748B] rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                    <div className="w-2 h-2 bg-[#64748B] rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                  </div>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Input Area */}
      <div className="border-t border-[#E2E8F0] p-4 bg-[#F8FAFC]">
        <div className="max-w-4xl mx-auto">
          <div className="flex gap-3 items-end">
            <div className="flex-1">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyPress}
                placeholder="Ask about your fleet..."
                className="w-full px-4 py-3 border border-[#E2E8F0] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#3B82F6] resize-none bg-white"
                rows={2}
                maxLength={2000}
              />
              <div className="flex items-center justify-between mt-1 px-1">
                <p className="text-xs text-[#94A3B8]">Press Enter to send</p>
                <p className="text-xs text-[#94A3B8]">{input.length} / 2000</p>
              </div>
            </div>
            <Button
              onClick={() => handleSend()}
              disabled={!input.trim() || loading}
              className="h-12"
            >
              <Send className="h-5 w-5" />
            </Button>
          </div>
          
          <p className="text-xs text-[#64748B] mt-3 text-center">
            FleetSync AI has access to your live fleet data including vehicles, drivers, trips, and maintenance records.
          </p>
        </div>
      </div>
    </div>
  );
}
